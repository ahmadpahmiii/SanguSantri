# 0002: AGP 9 built-in Kotlin, KSP instead of kapt

## Status

Accepted

## Context

The project bootstraps on Android Gradle Plugin 9.2.1, which enables built-in
Kotlin compilation by default and no longer requires (or supports alongside)
the `org.jetbrains.kotlin.android` plugin. Built-in Kotlin is also incompatible
with the `org.jetbrains.kotlin.kapt` plugin. Room and Hilt both need an
annotation-processing step.

## Decision

- Do not apply `org.jetbrains.kotlin.android`; rely on AGP's built-in Kotlin.
- Use KSP (`com.google.devtools.ksp`) for both Room's and Hilt's compilers
  instead of kapt. Room (`room-compiler`) and Hilt
  (`hilt-android-compiler`) both ship official KSP support.
- Pin the KSP Gradle plugin to `2.3.10`. Built-in Kotlin requires KSP `2.3.6`
  or newer — older, Kotlin-version-hyphenated KSP releases (for example
  `2.2.10-2.0.2`, the exact patch for this project's Kotlin version) register
  generated sources through the legacy `kotlin.sourceSets` DSL, which built-in
  Kotlin rejects with `EvalIssueException: Using kotlin.sourceSets DSL to add
  Kotlin sources is not allowed with built-in Kotlin`. KSP `2.3.0`+ dropped
  the Kotlin-version-hyphenated scheme and works across compatible Kotlin
  versions, including this project's `2.2.10`.
- Do not suppress the error via `android.disallowKotlinSourceSets=false` in
  `gradle.properties` — that papers over the real incompatibility instead of
  fixing it, and is called out explicitly as unsafe by the AGP 9 upgrade
  guidance.

## Consequences

- No `com.android.legacy-kapt` fallback is needed for the current dependency
  set. If a future dependency only ships a kapt processor, re-evaluate.
- KSP no longer needs to be bumped in lockstep with the Kotlin version now
  that both are on the decoupled scheme, but compatibility should still be
  re-verified on every Kotlin upgrade.
