package com.sangusantri.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One quiz-taking session (`0.0.5`). `currentQuestionIndex` always points at the next
 * not-yet-submitted question; it advances together with `correctCount` in the same atomic update
 * at submit time (`NahwuQuizDao.advanceAfterAnswer`), not on the later "Lanjut" tap — so a process
 * death between submitting an answer and tapping "Lanjut" only loses the just-answered question's
 * feedback screen, never the score itself. There is no separate per-question answer log: nothing
 * in the design spec's 15 screens requires reviewing an individual past answer, only the running
 * score and resume position, both of which this one row already carries.
 */
@Entity(
    tableName = "nahwu_quiz_attempts",
    foreignKeys = [
        ForeignKey(
            entity = NahwuQuizPackageEntity::class,
            parentColumns = ["id"],
            childColumns = ["packageId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["packageId"]), Index(value = ["completedAtEpochMillis"])],
)
data class NahwuQuizAttemptEntity(
    @PrimaryKey val id: String,
    val packageId: String,
    val startedAtEpochMillis: Long,
    val completedAtEpochMillis: Long?,
    val currentQuestionIndex: Int,
    val correctCount: Int,
    val totalCount: Int,
)
