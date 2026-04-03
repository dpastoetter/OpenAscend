# OpenAscend — release minification
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keepnames class kotlinx.serialization.** { *; }
-keepclassmembers class kotlinx.serialization.json.** { *; }
-dontwarn kotlinx.serialization.**

-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

-keep class com.openascend.domain.model.** { *; }
-keep class com.openascend.data.export.** { *; }

-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

-keep class androidx.health.connect.** { *; }
-dontwarn androidx.health.connect.**
