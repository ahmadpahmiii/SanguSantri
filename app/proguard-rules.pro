# SanguSantri R8 rules. Release builds run R8 in full mode (AGP 9, `optimization { enable = true }`).
#
# Until 2026-09-09 this file was empty *and* unreferenced — `app/build.gradle.kts` declared no
# `proguardFiles`, so only library consumer rules reached R8. Both were fixed together; a rule added
# here does nothing unless that `proguardFiles(...)` line stays.

##--------------------------------------------------------------------------------------------
## Retrofit response models (kotlinx.serialization)
##--------------------------------------------------------------------------------------------
# R8 full mode does not count "mentioned inside a Retrofit method's generic signature" as a use of a
# class. A response model reached *only* that way is deleted outright; the suspend function's
# response type — which lives in its `Continuation` parameter's signature, not its `Object` JVM
# return type — then erases to `Object`, and the converter asks kotlinx.serialization for a
# serializer for `Any`. That throws `IllegalArgumentException` on the main thread the first time the
# service interface is touched, so it is a hard crash rather than a failed request.
#
# This is not hypothetical. `PrayerEnvelopeDto` and `QuranEnvelopeDto` were both removed (they
# appear in `build/outputs/mapping/release/usage.txt`), crashing Kiblat in release on 2026-09-08.
# They became invisible to R8 when the 2026-09-08 data-layer refactor moved their only concrete
# readers behind the `ApiEnvelope` interface — before that, `KiblatRepositoryImpl` and
# `PrayerScheduleRepositoryImpl` read `.status`/`.data` directly, which kept them alive by accident.
#
# Keeping every `@Serializable` model in this app is the honest rule: each one exists precisely to
# be reflected over at runtime, so none of them is ever safe to shrink on reachability alone.
-keep @kotlinx.serialization.Serializable class com.sangusantri.app.** { *; }

# The generated serializer machinery those models reach reflectively.
-keepclassmembers class com.sangusantri.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.sangusantri.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.sangusantri.app.**$$serializer { *; }

# Generic signatures on the API interfaces themselves. Retrofit's own consumer rules keep
# `Signature`, `Continuation` and `Response`; this keeps the third participant — the interfaces'
# declared types — so the whole `Response<Envelope<Payload>>` chain survives intact.
-keep,allowobfuscation interface com.sangusantri.app.data.remote.**
