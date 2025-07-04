# PartyMaker - ProGuard Rules

# =================================
# BASIC ANDROID RULES
# =================================
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Keep Activities, Services, Receivers
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends androidx.fragment.app.Fragment

# Keep Google Play Services Location classes
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Keep SLF4J classes
-keep class org.slf4j.** { *; }
-dontwarn org.slf4j.**

# Keep companion objects
-keepclassmembers class * {
    ** Companion;
}

# Keep all companion object methods
-keepclassmembers class * {
    public static final ** Companion;
}

# =================================
# JETPACK COMPOSE
# =================================
-keep class androidx.compose.** { *; }
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-dontwarn androidx.compose.**

# =================================
# FIREBASE
# =================================
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Firebase Firestore specific
-keep class com.google.firebase.firestore.** { *; }
-keepnames class com.google.firebase.firestore.** { *; }

# Firebase Auth
-keep class com.google.firebase.auth.** { *; }
-keep class com.google.android.gms.auth.** { *; }

# =================================
# YOUR APP MODELS & DATA
# =================================
# Keep all your data classes (very important!)
-keep class com.example.partymaker.models.** { *; }
-keep class com.example.partymaker.data.** { *; }
-keep class com.example.partymaker.entities.** { *; }

# Keep classes with @Serializable annotation (if using)
-keep @kotlinx.serialization.Serializable class **

# =================================
# GOOGLE MAPS & LOCATION
# =================================
-keep class com.google.android.gms.maps.** { *; }
-keep class com.google.android.gms.location.** { *; }
-keep class com.google.android.libraries.places.** { *; }
-dontwarn com.google.android.gms.**

# =================================
# OPENAI & HTTP CLIENTS
# =================================
-keep class com.aallam.openai.** { *; }
-dontwarn com.aallam.openai.**

# OkHttp
-keep class okhttp3.** { *; }
-keep class okio.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# Ktor
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# JSON handling
-keep class org.json.** { *; }

# =================================
# PICASSO & IMAGE LOADING
# =================================
-keep class com.squareup.picasso.** { *; }
-dontwarn com.squareup.picasso.**

# CircleImageView
-keep class de.hdodenhof.circleimageview.** { *; }

# =================================
# KOTLIN SPECIFIC
# =================================
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}

# Kotlin Coroutines
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# =================================
# ENUMS & SERIALIZATION
# =================================
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# =================================
# REFLECTION SUPPORT
# =================================
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleParameterAnnotations

# =================================
# REMOVE LOGS IN RELEASE
# =================================
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int d(...);
    public static int w(...);
    public static int v(...);
    public static int i(...);
}

# =================================
# CRASHLYTICS (if we add it later)
# =================================
# -keep class com.google.firebase.crashlytics.** { *; }
# -dontwarn com.google.firebase.crashlytics.**