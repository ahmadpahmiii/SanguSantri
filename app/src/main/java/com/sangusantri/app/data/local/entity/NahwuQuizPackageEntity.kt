package com.sangusantri.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** One bundled Nahwu Quiz question package (`0.0.5`) — local mirror of a bundled
 * `assets/nahwu_quiz/nahwu_quiz_bank.json` package entry, no remote sync (roadmap scope: bundled
 * static JSON question bank only). */
@Entity(tableName = "nahwu_quiz_packages")
data class NahwuQuizPackageEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val order: Int,
    val isActive: Boolean,
)
