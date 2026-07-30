# Add project specific ProGuard rules here.

# Tink (via androidx.security.crypto's EncryptedSharedPreferences) references Error Prone's
# compile-time-only annotations; they're absent at runtime by design, R8 just needs telling
# that's expected instead of failing the build over it.
-dontwarn com.google.errorprone.annotations.CanIgnoreReturnValue
-dontwarn com.google.errorprone.annotations.CheckReturnValue
-dontwarn com.google.errorprone.annotations.Immutable
-dontwarn com.google.errorprone.annotations.RestrictedApi

-keep,allowobfuscation,allowshrinking class kotlinx.serialization.** { *; }
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keep,includedescriptorclasses class dev.ividi.weatherapp.**$$serializer { *; }
-keepclassmembers class dev.ividi.weatherapp.** {
    *** Companion;
}
-keepclasseswithmembers class dev.ividi.weatherapp.** {
    kotlinx.serialization.KSerializer serializer(...);
}
