package com.sangusantri.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sangusantri.app.domain.model.NahwuQuizOptionKey

/** One bundled multiple-choice question (`0.0.5`) — always exactly four flat option columns
 * (never a separate options table/join, per the design spec's fixed four-option layout). */
@Entity(
    tableName = "nahwu_quiz_questions",
    foreignKeys = [
        ForeignKey(
            entity = NahwuQuizPackageEntity::class,
            parentColumns = ["id"],
            childColumns = ["packageId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["packageId"])],
)
data class NahwuQuizQuestionEntity(
    @PrimaryKey val id: String,
    val packageId: String,
    val order: Int,
    val stem: String,
    val optionAText: String,
    val optionBText: String,
    val optionCText: String,
    val optionDText: String,
    val correctOption: NahwuQuizOptionKey,
    val explanation: String?,
)
