# Keep the @Serializable annotated classes (Kotlinx Serialization)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keep,includedescriptorclasses class com.example.epicinv.**$$serializer { *; }
-keepclassmembers class com.example.epicinv.** {
    *** Companion;
}
-keepclasseswithmembers class com.example.epicinv.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**

# Glance
-keep class androidx.glance.** { *; }

# Coil
-keep class coil.** { *; }

# Kotlin reflect
-dontwarn kotlin.reflect.**
