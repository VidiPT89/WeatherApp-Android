# Add project specific ProGuard rules here.
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
