package com.sangusantri.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sangusantri.app.domain.model.ContentLayout

/** One catalog item — local mirror of a CMS API catalog entry. */
@Entity(tableName = "content")
data class ContentEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val category: String?,
    val version: Int,
    val order: Int,
    val isActive: Boolean,
    val sourceName: String,
    val sourceUrl: String,
    /**
     * Defaulted so the CMS *list* import — which carries no layout, and must never overwrite a
     * known-good value with a default — constructs this row without naming it. Only the detail
     * import sets it, because only the detail carries it.
     */
    val layout: ContentLayout = ContentLayout.STACKED,
)
