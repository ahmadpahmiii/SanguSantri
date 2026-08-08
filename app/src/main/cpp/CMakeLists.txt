# Native credential-reconstruction boundary for the standalone Al-Qur'an Kemenag feature (ADR 0016).
# Deliberately tiny: one translation unit, no third-party dependencies. It exists to raise the cost
# of extracting the Kemenag `username`/`token` from a release APK, never to claim the token is kept
# secret from a determined attacker (docs/security/SECURITY_BASELINE.md).
cmake_minimum_required(VERSION 3.22.1)

project(qurancredential)

if (NOT DEFINED GENERATED_HEADER_DIR)
    message(FATAL_ERROR
            "GENERATED_HEADER_DIR was not passed by Gradle — app/build.gradle.kts's "
            "generateQuranCredentialHeader task must run before this CMake configure step.")
endif ()

add_library(qurancredential SHARED quran_credential.cpp)

target_include_directories(qurancredential PRIVATE ${GENERATED_HEADER_DIR})

# C++17 is available on every NDK toolchain this project targets (minSdk 26).
target_compile_features(qurancredential PRIVATE cxx_std_17)
