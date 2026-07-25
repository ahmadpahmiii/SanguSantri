package com.sangusantri.app.data.local.seed

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sangusantri.app.data.local.dao.AmaliyahDao
import com.sangusantri.app.data.local.dao.AmaliyahStepDao
import com.sangusantri.app.data.local.dao.AmaliyahVariantDao
import com.sangusantri.app.data.local.dao.AmaliyahVersionDao
import com.sangusantri.app.data.local.dao.ApprovalDao
import com.sangusantri.app.data.local.database.SanguSantriDatabase
import com.sangusantri.app.data.local.entity.AmaliyahEntity
import com.sangusantri.app.data.local.entity.AmaliyahStepEntity
import com.sangusantri.app.data.local.entity.AmaliyahVariantEntity
import com.sangusantri.app.data.local.entity.AmaliyahVersionEntity
import com.sangusantri.app.data.local.entity.ApprovalEntity
import com.sangusantri.app.data.local.seed.dto.AmaliyahDto
import com.sangusantri.app.data.local.seed.dto.AmaliyahStepDto
import com.sangusantri.app.data.local.seed.dto.AmaliyahVariantDto
import com.sangusantri.app.data.local.seed.dto.AmaliyahVersionDto
import com.sangusantri.app.data.local.seed.dto.ApprovalDto
import com.sangusantri.app.data.local.seed.dto.ContentManifestDto
import com.sangusantri.app.data.local.seed.dto.ContentManifestEntryDto
import com.sangusantri.app.data.local.seed.dto.ContentPackageDto
import com.sangusantri.app.domain.model.AmaliyahVersionStatus
import com.sangusantri.app.domain.model.ApprovalStatus
import com.sangusantri.app.domain.model.OwnerType
import com.sangusantri.app.domain.model.StepType
import com.sangusantri.app.domain.model.Visibility
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SeedContentImporterTest {
    private lateinit var database: SanguSantriDatabase
    private lateinit var amaliyahDao: AmaliyahDao
    private lateinit var amaliyahVariantDao: AmaliyahVariantDao
    private lateinit var approvalDao: ApprovalDao
    private lateinit var amaliyahVersionDao: AmaliyahVersionDao
    private lateinit var amaliyahStepDao: AmaliyahStepDao
    private val json = Json { ignoreUnknownKeys = true }

    @Before
    fun createDatabase() {
        database =
            Room
                .inMemoryDatabaseBuilder(
                    ApplicationProvider.getApplicationContext(),
                    SanguSantriDatabase::class.java,
                ).build()
        amaliyahDao = database.amaliyahDao()
        amaliyahVariantDao = database.amaliyahVariantDao()
        approvalDao = database.approvalDao()
        amaliyahVersionDao = database.amaliyahVersionDao()
        amaliyahStepDao = database.amaliyahStepDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun firstImportInsertsBothBundledFixturePackages() =
        runTest {
            val outcomes = importer(realAssetSource()).importSeedContent()

            assertTrue(outcomes.all { it is SeedImportOutcome.Imported })
            assertEquals(2, amaliyahDao.observeAll().first().size)
            assertEquals(8, amaliyahStepDao.countByVersionId("tahlil-umum-v1"))
            assertEquals(8, amaliyahStepDao.countByVersionId("istighosah-umum-v1"))
        }

    @Test
    fun duplicateImportIsIdempotentAndWritesNoExtraRows() =
        runTest {
            val underTest = importer(realAssetSource())
            underTest.importSeedContent()

            val secondRun = underTest.importSeedContent()

            assertTrue(secondRun.all { it is SeedImportOutcome.AlreadyImported })
            assertEquals(2, amaliyahDao.observeAll().first().size)
            assertEquals(8, amaliyahStepDao.countByVersionId("tahlil-umum-v1"))
        }

    @Test
    fun invalidChecksumRejectsThePackageAndWritesNothing() =
        runTest {
            val pkg = validPackage()
            val source = fakeSourceFor(pkg, checksumOverride = "0".repeat(64))

            val outcomes = importer(source).importSeedContent()

            assertEquals(1, outcomes.size)
            assertTrue(outcomes.first() is SeedImportOutcome.Failed)
            assertEquals(0, amaliyahDao.observeAll().first().size)
            assertNull(amaliyahVersionDao.getById(pkg.version.id))
        }

    @Test
    fun databaseConstraintFailureMidImportRollsBackTheWholePackage() =
        runTest {
            val pkg = validPackage()
            seedPreExistingStep(id = pkg.steps.first().id)

            val outcomes = importer(fakeSourceFor(pkg)).importSeedContent()

            assertTrue(outcomes.first() is SeedImportOutcome.Failed)
            assertNull(amaliyahDao.getBySlug(pkg.amaliyah.slug))
            assertNull(amaliyahVersionDao.getById(pkg.version.id))
        }

    private fun importer(source: SeedContentSource) =
        SeedContentImporter(seedContentSource = source, database = database)

    private fun realAssetSource(): AssetSeedContentSource =
        AssetSeedContentSource(InstrumentationRegistry.getInstrumentation().targetContext)

    private fun fakeSourceFor(
        pkg: ContentPackageDto,
        checksumOverride: String? = null,
    ): FakeSeedContentSource {
        val packageBytes = json.encodeToString(pkg).encodeToByteArray()
        val checksum = checksumOverride ?: SeedContentChecksum.sha256Hex(packageBytes)
        val manifest =
            ContentManifestDto(
                schemaVersion = 1,
                generatedAt = "2026-07-25T00:00:00Z",
                packages =
                    listOf(
                        ContentManifestEntryDto(
                            versionId = pkg.version.id,
                            file = "pkg.json",
                            checksumSha256 = checksum,
                        ),
                    ),
            )
        return FakeSeedContentSource(
            manifestBytes = json.encodeToString(manifest).encodeToByteArray(),
            packages = mapOf("pkg.json" to packageBytes),
        )
    }

    /** Inserts an unrelated version to own a pre-existing step row, forcing a PK conflict later. */
    private suspend fun seedPreExistingStep(id: String) {
        amaliyahDao.insert(
            AmaliyahEntity(
                id = "other",
                slug = "other",
                titleId = "Other",
                titleAr = "[FIXTURE-AR]",
                descriptionId = null,
                descriptionAr = null,
                category = "AMALIYAH",
            ),
        )
        amaliyahVariantDao.insert(
            AmaliyahVariantEntity(
                id = "other-umum",
                amaliyahId = "other",
                slug = "umum",
                nameId = "Umum",
                nameAr = "[FIXTURE-AR]",
                ownerType = OwnerType.PUBLIC,
                pondokId = null,
                visibility = Visibility.PUBLIC,
                isDefault = true,
            ),
        )
        approvalDao.insert(
            ApprovalEntity(
                id = "other-approval",
                approverName = "NON-PRODUCTION FIXTURE",
                approverRole = "N/A",
                institutionName = null,
                approvalDate = "2026-07-25",
                approvalScope = "N/A",
                publicDocumentStorageKey = null,
                documentReferenceNumber = null,
                status = ApprovalStatus.PENDING,
            ),
        )
        amaliyahVersionDao.insert(
            AmaliyahVersionEntity(
                id = "other-v1",
                variantId = "other-umum",
                versionNumber = 1,
                schemaVersion = 1,
                status = AmaliyahVersionStatus.DRAFT,
                sourceName = "NON-PRODUCTION FIXTURE",
                sourceReference = "N/A",
                approvalId = "other-approval",
                checksumSha256 = "0".repeat(64),
                minimumAppVersionCode = 1,
                publishedAt = null,
                revokedAt = null,
            ),
        )
        amaliyahStepDao.insertAll(
            listOf(
                AmaliyahStepEntity(
                    id = id,
                    versionId = "other-v1",
                    position = 1,
                    stepType = StepType.DIVIDER,
                    titleId = null,
                    titleAr = null,
                    arabicText = null,
                    translationId = null,
                    instructionId = null,
                    instructionAr = null,
                    repeatTarget = null,
                    quranSurahNumber = null,
                    quranAyahStart = null,
                    quranAyahEnd = null,
                    audioGroupId = null,
                ),
            ),
        )
    }

    private fun validPackage(): ContentPackageDto =
        ContentPackageDto(
            schemaVersion = SeedContentValidator.SUPPORTED_SCHEMA_VERSION,
            amaliyah =
                AmaliyahDto(
                    id = "sample",
                    slug = "sample",
                    titleId = "Sample",
                    titleAr = "[FIXTURE-AR]",
                    category = "AMALIYAH",
                ),
            variant =
                AmaliyahVariantDto(
                    id = "sample-umum",
                    slug = "umum",
                    nameId = "Umum",
                    nameAr = "[FIXTURE-AR]",
                    ownerType = OwnerType.PUBLIC,
                    visibility = Visibility.PUBLIC,
                    isDefault = true,
                ),
            version =
                AmaliyahVersionDto(
                    id = "sample-umum-v1",
                    versionNumber = 1,
                    status = AmaliyahVersionStatus.DRAFT,
                    sourceName = "NON-PRODUCTION FIXTURE",
                    sourceReference = "N/A",
                    minimumAppVersionCode = 1,
                ),
            approval =
                ApprovalDto(
                    id = "sample-umum-v1-approval",
                    approverName = "NON-PRODUCTION FIXTURE",
                    approverRole = "N/A",
                    approvalDate = "2026-07-25",
                    approvalScope = "N/A",
                    status = ApprovalStatus.PENDING,
                ),
            steps =
                listOf(
                    AmaliyahStepDto(
                        id = "sample-umum-v1-step-01",
                        position = 1,
                        stepType = StepType.HEADING,
                        titleId = "Pembukaan",
                    ),
                ),
        )

    private class FakeSeedContentSource(
        private val manifestBytes: ByteArray,
        private val packages: Map<String, ByteArray>,
    ) : SeedContentSource {
        override fun readManifest(): ByteArray = manifestBytes

        override fun readPackage(fileName: String): ByteArray = packages.getValue(fileName)
    }
}
