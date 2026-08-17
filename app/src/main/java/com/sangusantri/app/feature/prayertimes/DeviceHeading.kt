package com.sangusantri.app.feature.prayertimes

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/** Where the device is pointing, and how much that reading can be trusted. */
data class DeviceHeading(
    /** Degrees clockwise from magnetic north, `0f..360f`. */
    val azimuthDegrees: Float,
    /** `true` while the magnetometer reports low or unreliable accuracy — the moment to show the
     * figure-of-eight calibration hint rather than pretending the needle is exact. */
    val needsCalibration: Boolean,
)

/**
 * Live compass heading from the rotation-vector sensor, or `null` on a device without one (many
 * tablets and emulators) so the caller can fall back to a static needle instead of a stuck one.
 *
 * Uses `TYPE_ROTATION_VECTOR` rather than raw accelerometer + magnetometer because the platform
 * already fuses those, including the gyroscope where present — that fusion is what makes the needle
 * steady instead of jittery. No permission is involved; sensors are not a protected resource.
 *
 * Readings are low-pass filtered: a raw magnetometer stream visibly shakes, and a qibla needle that
 * trembles reads as broken even when it is accurate.
 */
@Composable
fun rememberDeviceHeading(): DeviceHeading? {
    val context = LocalContext.current
    val sensorManager = remember { ContextCompat.getSystemService(context, SensorManager::class.java) }
    val rotationSensor = remember(sensorManager) { sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) }
    var heading by remember { mutableStateOf<DeviceHeading?>(null) }

    DisposableEffect(sensorManager, rotationSensor) {
        if (sensorManager == null || rotationSensor == null) return@DisposableEffect onDispose {}

        val rotationMatrix = FloatArray(MATRIX_SIZE)
        val orientation = FloatArray(ORIENTATION_SIZE)
        var smoothed: Float? = null
        var unreliable = false

        val listener =
            object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    SensorManager.getOrientation(rotationMatrix, orientation)
                    val raw = Math.toDegrees(orientation[0].toDouble()).toFloat().normalizeDegrees()
                    smoothed = smoothed?.let { previous -> previous.smoothedTowards(raw) } ?: raw
                    heading = DeviceHeading(azimuthDegrees = smoothed ?: raw, needsCalibration = unreliable)
                }

                override fun onAccuracyChanged(
                    sensor: Sensor?,
                    accuracy: Int,
                ) {
                    unreliable = accuracy <= SensorManager.SENSOR_STATUS_ACCURACY_LOW
                }
            }

        sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        onDispose { sensorManager.unregisterListener(listener) }
    }
    return heading
}

/** Exponential smoothing that crosses 0°/360° the short way instead of spinning the long way. */
private fun Float.smoothedTowards(target: Float): Float {
    val delta = ((target - this + HALF_TURN + FULL_TURN) % FULL_TURN) - HALF_TURN
    return (this + delta * SMOOTHING_FACTOR).normalizeDegrees()
}

internal fun Float.normalizeDegrees(): Float = ((this % FULL_TURN) + FULL_TURN) % FULL_TURN

private const val MATRIX_SIZE = 9
private const val ORIENTATION_SIZE = 3
private const val SMOOTHING_FACTOR = 0.18f
internal const val FULL_TURN = 360f
internal const val HALF_TURN = 180f
