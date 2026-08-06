package com.sangusantri.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** One ordered reading step within a [ContentEntity] (ADR 0015). */
@Entity(
    tableName = "content_steps",
    foreignKeys = [
        ForeignKey(
            entity = ContentEntity::class,
            parentColumns = ["id"],
            childColumns = ["contentId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["contentId"]),
        Index(value = ["contentId", "position"], unique = true),
    ],
)
data class ContentStepEntity(
    @PrimaryKey val id: String,
    val contentId: String,
    val position: Int,
    val arabicText: String,
    val translation: String,
    val repeatTarget: Int,
)
