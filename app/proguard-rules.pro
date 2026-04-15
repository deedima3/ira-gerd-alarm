# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep attributes for debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep AlarmReceiver and related classes
-keep class com.example.cutesyalarm.receiver.** { *; }
-keep class com.example.cutesyalarm.model.** { *; }
-keep class com.example.cutesyalarm.service.** { *; }
-keep class com.example.cutesyalarm.util.UpdateManager$UpdateInfo { *; }
-keep class com.example.cutesyalarm.BuildConfig { *; }
-keep class com.example.cutesyalarm.AlarmRingingActivity { *; }

# Keep Compose
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }

# Keep Navigation
-keep class androidx.navigation.** { *; }

# Keep WorkManager
-keep class androidx.work.** { *; }

# Keep DataStore
-keep class androidx.datastore.** { *; }

# Keep FileProvider
-keep class androidx.core.content.FileProvider { *; }

# Keep Kotlin metadata
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleParameterAnnotations

# Keep Kotlin
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# Keep custom exceptions
-keep public class * extends java.lang.Exception