# Add project specific ProGuard rules here.

# Retrofit + OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keepattributes *Annotation*

# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class **$$serializer { *; }
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    *** serializer(...);
}

# Hilt
-keepclasseswithmembernames class * { @dagger.hilt.* *; }
