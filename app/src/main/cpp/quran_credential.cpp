// Native credential-reconstruction boundary for the standalone Al-Qur'an Kemenag feature
// (ADR 0016, docs/security/SECURITY_BASELINE.md). Deliberately minimal: verify the caller's own
// release signing-certificate digest, then XOR-decode a build-time-embedded credential. This is
// defence-in-depth against casual static extraction — it raises the cost of recovering the
// Kemenag `username`/`token` from a release APK, but a determined attacker with the device and APK
// can still ultimately recover it (an explicitly accepted, documented risk, never described here
// or anywhere else in this codebase as making the credential secret).
//
// quran_credential_secrets.h is GENERATED at build time by :app's generateQuranCredentialHeader
// Gradle task from an untracked local/CI secret (never committed, never part of this source file)
// and is not present in this directory — see app/build.gradle.kts.
#include <jni.h>

#include <cstring>
#include <string>

#include "quran_credential_secrets.h"

namespace {

    // Checked against every configured digest (build/generated .../quran_credential_secrets.h) —
    // Google re-signs the same upload differently per distribution path (real Play App Signing key
    // for Play Store installs, a separate Internal App Sharing test certificate for share links),
    // so more than one digest can be legitimate. Each candidate still requires an exact match; nothing
    // here is more permissive per-candidate, only more digests are considered.
    bool SigningDigestMatches(JNIEnv *env, jbyteArray actual) {
        if (!kSigningDigestConfigured || actual == nullptr) {
            return false;
        }
        const jsize length = env->GetArrayLength(actual);
        if (static_cast<size_t>(length) != kSigningDigestLength) {
            return false;
        }
        jbyte *bytes = env->GetByteArrayElements(actual, nullptr);
        if (bytes == nullptr) {
            return false;
        }
        bool matches = false;
        for (size_t i = 0; i < kExpectedSigningDigestCount; ++i) {
            if (std::memcmp(bytes, kExpectedSigningSha256[i], kSigningDigestLength) == 0) {
                matches = true;
                break;
            }
        }
        env->ReleaseByteArrayElements(actual, bytes, JNI_ABORT);
        return matches;
    }

    std::string XorDecode(const unsigned char *encoded, size_t length) {
        std::string decoded;
        decoded.reserve(length);
        for (size_t i = 0; i < length; ++i) {
            decoded.push_back(static_cast<char>(encoded[i] ^ kCredentialXorKey));
        }
        return decoded;
    }

}  // namespace

extern "C" JNIEXPORT jstring JNICALL
Java_com_sangusantri_app_data_remote_quran_QuranNativeCredentialBridge_nativeGetCredential(
        JNIEnv *env, jobject /* bridge */, jbyteArray signing_certificate_sha256) {
    if (!kCredentialConfigured) {
        return nullptr;  // no build-time secret available (local/debug build) — fail closed
    }
    if (!SigningDigestMatches(env, signing_certificate_sha256)) {
        return nullptr;  // fail closed: never reveal expected-vs-actual, never log
    }

    const std::string username = XorDecode(kEncodedUsername, kEncodedUsernameLength);
    const std::string token = XorDecode(kEncodedToken, kEncodedTokenLength);
    // U+0001 delimiter — matches QuranNativeCredentialBridge.CREDENTIAL_SEPARATOR on the Kotlin side.
    const std::string combined = username + "\x01" + token;
    return env->NewStringUTF(combined.c_str());
}
