package com.sangusantri.app.data.repository

import com.sangusantri.app.data.local.dao.AmaliyahDao
import com.sangusantri.app.data.local.dao.AmaliyahStepDao
import com.sangusantri.app.data.local.dao.AmaliyahVariantDao
import com.sangusantri.app.data.local.dao.AmaliyahVersionDao
import com.sangusantri.app.data.local.dao.ApprovalDao
import com.sangusantri.app.data.mapper.toDomain
import com.sangusantri.app.domain.model.Amaliyah
import com.sangusantri.app.domain.model.AmaliyahVersionDetail
import com.sangusantri.app.domain.repository.ContentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Reads the amaliyah catalogue from Room, the local source of truth (PRD 12.1). */
class ContentRepositoryImpl
    @Inject
    constructor(
        private val amaliyahDao: AmaliyahDao,
        private val amaliyahVariantDao: AmaliyahVariantDao,
        private val approvalDao: ApprovalDao,
        private val amaliyahVersionDao: AmaliyahVersionDao,
        private val amaliyahStepDao: AmaliyahStepDao,
    ) : ContentRepository {
        override fun observeAmaliyah(): Flow<List<Amaliyah>> =
            amaliyahDao.observeAll().map { list ->
                list.map { it.toDomain() }
            }

    override suspend fun getAmaliyahBySlug(amaliyahSlug: String): Amaliyah? =
        amaliyahDao.getBySlug(amaliyahSlug)?.toDomain()

        // Four sequential, independent lookups that each short-circuit to "not found" — flat
        // guard clauses read more clearly here than the nested `?.let` chain the alternative forces.
        @Suppress("ReturnCount")
        override suspend fun getDefaultVersionDetail(amaliyahSlug: String): AmaliyahVersionDetail? {
            val amaliyah = amaliyahDao.getBySlug(amaliyahSlug) ?: return null
            val variant = amaliyahVariantDao.getDefaultForAmaliyah(amaliyah.id) ?: return null
            val version = amaliyahVersionDao.getLatestPublishedForVariant(variant.id) ?: return null
            val approval = approvalDao.getById(version.approvalId) ?: return null

            return AmaliyahVersionDetail(
                version = version.toDomain(),
                approval = approval.toDomain(),
                steps = amaliyahStepDao.getByVersionId(version.id).map { it.toDomain() },
            )
        }
    }
