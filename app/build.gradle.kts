plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
    alias(libs.plugins.google.firebase.appdistribution)
}

// Generated native build input for the Quran credential boundary (ADR 0016) — written by
// generateQuranCredentialHeader below, outside the tracked source tree, never committed.
val quranCredentialHeaderDir = layout.buildDirectory.dir("generated/quranCredential")

android {
    namespace = "com.sangusantri.app"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.sangusantri.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 9
        versionName = "0.0.4"

        testInstrumentationRunner = "com.sangusantri.app.HiltTestRunner"

        // Content sync backend base URL (section 8): a Gradle/CI property activates the real
        // backend without any code change or source-selection flag. The `.invalid` default keeps
        // the project buildable with no real backend configured — a non-routable TLD per RFC 2606.
        val contentApiBaseUrl =
            (project.findProperty("SANGU_CONTENT_API_BASE_URL") as String?)
                ?: "https://content-api.sangusantri.invalid/"
        buildConfigField("String", "CONTENT_API_BASE_URL", "\"$contentApiBaseUrl\"")

        // Official LPMQ Kemenag Quran API base URL (0.0.6, ADR 0016) — fixed and publicly
        // documented (docs/product/QURAN_PRD.md §3), not a secret and not environment-specific, so
        // unlike CONTENT_API_BASE_URL above it has no override property.
        buildConfigField("String", "QURAN_API_BASE_URL", "\"https://quran-api.lpmqkemenag.id/api-alquran/\"")

        externalNativeBuild {
            cmake {
                arguments += "-DGENERATED_HEADER_DIR=${quranCredentialHeaderDir.get().asFile.absolutePath}"
            }
        }
        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64", "armeabi-v7a", "x86")
        }
    }

    ndkVersion = "27.1.12297006"
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    buildTypes {
        debug {
            // Optional untracked local override so a developer can exercise the real Kemenag API
            // from a debug build without touching the native release credential path (ADR 0016
            // amendment 2026-08-09). Absent by default — QuranCredentialProvider falls back to the
            // fake fixture credential when either value is blank. Never read from the tracked
            // gradle.properties, never logged, never present in a release BuildConfig.
            buildConfigField(
                "String",
                "QURAN_DEBUG_API_USERNAME",
                "\"${quranSecretProperty("SANGU_QURAN_DEBUG_API_USERNAME") ?: ""}\"",
            )
            buildConfigField(
                "String",
                "QURAN_DEBUG_API_TOKEN",
                "\"${quranSecretProperty("SANGU_QURAN_DEBUG_API_TOKEN") ?: ""}\"",
            )
        }
        release {
            optimization {
                enable = true
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
}

// --- Quran Kemenag credential boundary (ADR 0016) ---------------------------------------------
//
// Reads the real Kemenag `username`/`token` and this app's expected release signing-certificate
// SHA-256 digest from an untracked local/CI secret (environment variable or Gradle property —
// never the tracked gradle.properties/local.properties, and never Kotlin/XML source). Absent
// values are safe here: they produce an "unconfigured" native header that always fails closed at
// runtime, so a local debug build with no secrets configured still compiles and installs.
// [verifyQuranReleaseCredential] below is the separate, hard gate that fails a *release* assembly
// clearly when these are missing, per docs/security/SECURITY_BASELINE.md.
fun quranSecretProperty(name: String): String? =
    (System.getenv(name) ?: project.findProperty(name) as String?)?.takeIf { it.isNotBlank() }

fun quranHexDigestToBytes(hex: String): ByteArray {
    val cleaned = hex.replace(":", "").replace(" ", "")
    require(cleaned.length == 64) {
        "SANGU_QURAN_RELEASE_SHA256 must be a comma-separated list of 64-hex-character SHA-256 " +
            "digests, got a ${cleaned.length}-char entry"
    }
    return ByteArray(32) { i -> cleaned.substring(i * 2, i * 2 + 2).toInt(16).toByte() }
}

// Google re-signs the same app differently depending on distribution path — the real Play App
// Signing key for actual Play Store installs, but a separate, Google-generated "Internal test
// certificate" for Internal App Sharing links (confirmed 2026-08-09 in production: an app-sharing
// install's runtime signing-certificate digest never matches the real App Signing Key). A
// comma-separated list lets both be accepted without weakening the check itself — each candidate
// still requires an exact match; this only widens which of Google's own re-signing certificates
// count as legitimate.
fun quranHexDigestListToBytesList(commaSeparatedHex: String): List<ByteArray> =
    commaSeparatedHex
        .split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map(::quranHexDigestToBytes)

fun quranCByteArrayLiteral(bytes: ByteArray): String =
    bytes.joinToString(prefix = "{", postfix = "}", separator = ", ") { "0x%02X".format(it.toInt() and 0xFF) }

fun quranCredentialHeaderContent(): String {
    val username = quranSecretProperty("SANGU_QURAN_API_USERNAME")
    val token = quranSecretProperty("SANGU_QURAN_API_TOKEN")
    val digestHex = quranSecretProperty("SANGU_QURAN_RELEASE_SHA256")

    val credentialConfigured = username != null && token != null
    val xorKey = (1..255).random()

    fun encode(value: String) = value.toByteArray(Charsets.UTF_8).map { (it.toInt() xor xorKey).toByte() }.toByteArray()
    val encodedUsername = if (credentialConfigured) encode(username!!) else byteArrayOf(0)
    val encodedToken = if (credentialConfigured) encode(token!!) else byteArrayOf(0)

    val digestConfigured = digestHex != null
    val digestList: List<ByteArray> =
        if (digestConfigured) quranHexDigestListToBytesList(digestHex!!) else listOf(ByteArray(32))

    return buildString {
        appendLine("// GENERATED FILE — do not edit or commit (build/ is gitignored).")
        appendLine("// Produced by :app's generateQuranCredentialHeader Gradle task from untracked local/CI")
        appendLine("// secrets (ADR 0016). Absent inputs produce an unconfigured/all-zero placeholder so a")
        appendLine("// local debug build compiles without secrets — release assembly is separately gated by")
        appendLine("// verifyQuranReleaseCredential.")
        appendLine("#pragma once")
        appendLine("#include <cstddef>")
        appendLine()
        appendLine("static const bool kCredentialConfigured = $credentialConfigured;")
        appendLine("static const unsigned char kCredentialXorKey = 0x%02X;".format(xorKey))
        appendLine("static const unsigned char kEncodedUsername[] = ${quranCByteArrayLiteral(encodedUsername)};")
        appendLine(
            "static const size_t kEncodedUsernameLength = ${if (credentialConfigured) encodedUsername.size else 0};",
        )
        appendLine("static const unsigned char kEncodedToken[] = ${quranCByteArrayLiteral(encodedToken)};")
        appendLine("static const size_t kEncodedTokenLength = ${if (credentialConfigured) encodedToken.size else 0};")
        appendLine("static const bool kSigningDigestConfigured = $digestConfigured;")
        appendLine("static const size_t kSigningDigestLength = 32;")
        appendLine("static const size_t kExpectedSigningDigestCount = ${digestList.size};")
        appendLine(
            "static const unsigned char kExpectedSigningSha256[${digestList.size}][32] = " +
                digestList.joinToString(prefix = "{", postfix = "}", separator = ", ") { quranCByteArrayLiteral(it) } +
                ";",
        )
    }
}

// Computed at configuration time (not inside doLast) so the task action captures a plain String,
// never a Project/script object reference — required for Gradle configuration-cache compatibility.
val quranCredentialHeaderText = quranCredentialHeaderContent()

val generateQuranCredentialHeader by tasks.registering {
    description = "Writes the generated native credential header consumed by app/src/main/cpp (ADR 0016)."
    val outputDir = quranCredentialHeaderDir
    val headerText = quranCredentialHeaderText
    outputs.dir(outputDir)
    doLast {
        val dir = outputDir.get().asFile
        dir.mkdirs()
        dir.resolve("quran_credential_secrets.h").writeText(headerText)
    }
}

// CMake configure/build tasks (name pattern "{configure|build}CMake{BuildType}[{ABI}]") must not
// run before the header they #include exists.
tasks.matching { it.name.contains("CMake") }.configureEach { dependsOn(generateQuranCredentialHeader) }

// Computed at configuration time for the same configuration-cache-compatibility reason as
// quranCredentialHeaderText above.
val quranMissingReleaseCredentialProperties =
    listOfNotNull(
        "SANGU_QURAN_API_USERNAME".takeIf { quranSecretProperty(it) == null },
        "SANGU_QURAN_API_TOKEN".takeIf { quranSecretProperty(it) == null },
        "SANGU_QURAN_RELEASE_SHA256".takeIf { quranSecretProperty(it) == null },
    )

val verifyQuranReleaseCredential by tasks.registering {
    description = "Fails the build clearly when the release Kemenag credential/signing digest are absent (ADR 0016)."
    val missing = quranMissingReleaseCredentialProperties
    doLast {
        if (missing.isNotEmpty()) {
            throw GradleException(
                "Release build requires the Kemenag Quran credential and release signing-certificate " +
                    "digest (ADR 0016). Missing: ${missing.joinToString(", ")}. Supply them as environment " +
                    "variables or Gradle properties from untracked local/CI secret storage — never commit " +
                    "them to source control.",
            )
        }
    }
}

tasks.matching { it.name == "assembleRelease" || it.name == "bundleRelease" }.configureEach {
    dependsOn(verifyQuranReleaseCredential)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    autoCorrect = false
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // Extended icon set (justified by the full approved 0.0.2-0.0.5 scope's broad icon
    // vocabulary — history, restart_alt, check_circle, notifications, calendar_month, etc. — not
    // "a couple of icons"; R8 resource/code shrinking is already enabled so unused icons are
    // stripped from the release APK).
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.firebase.crashlytics)

    // In-app update (ADR 0017): Remote Config supplies the force/flexible policy, Play Core drives
    // the actual update flow.
    implementation(libs.firebase.config)
    implementation(libs.play.app.update)
    implementation(libs.play.app.update.ktx)

    // Catalog item images (ADR 0015 — Content.imageUrl); network fetcher shares the app's own
    // OkHttp stack rather than pulling in a second HTTP client.
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // Dependency injection
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.hilt.android.compiler)

    // Local persistence
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)

    // Navigation
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.kotlinx.serialization.json)

    // Networking (remote content manifest/package sync)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp)

    // In-app HTTP inspector (debug only) — same API in both artifacts, so call sites never branch
    // on build type; library-no-op is a stub with every method a no-op, keeping it out of release.
    debugImplementation(libs.chucker.library)
    releaseImplementation(libs.chucker.library.no.op)

    // Background sync
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.hilt.android.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.work.testing)
    androidTestImplementation(libs.okhttp.mockwebserver)
    kspAndroidTest(libs.hilt.android.compiler)

    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
