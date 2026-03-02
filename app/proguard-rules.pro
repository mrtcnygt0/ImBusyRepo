# ProGuard rules for Şu an Meşgulüm
# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# ONNX Runtime
-keep class ai.onnxruntime.** { *; }

# Billing
-keep class com.android.vending.billing.**

# Keep data classes
-keep class com.suanmesgulum.app.data.local.entity.** { *; }
-keep class com.suanmesgulum.app.domain.model.** { *; }
