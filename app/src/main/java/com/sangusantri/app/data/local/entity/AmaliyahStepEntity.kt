package com.sangusantri.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sangusantri.app.domain.model.StepType

/** Local mirror of the server `amaliyah_steps` table (PRD 10.2, 11.1). */
@Entity(
    tableName = "amaliyah_steps",
    foreignKeys = [
        ForeignKey(
            entity = AmaliyahVersionEntity::class,
            parentColumns = ["id"],
            childColumns = ["versionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["versionId"]),
        Index(value = ["versionId", "position"], unique = true),
    ],
)
data class AmaliyahStepEntity(
    @PrimaryKey val id: String,
    val versionId: String,
    val position: Int,
    val stepType: StepType,
    val titleId: String?,
    val titleAr: String?,
    val arabicText: String?,
    val translationId: String?,
    val instructionId: String?,
    val instructionAr: String?,
    val repeatTarget: Int?,
    val quranSurahNumber: Int?,
    val quranAyahStart: Int?,
    val quranAyahEnd: Int?,
    val audioGroupId: String?,
)
