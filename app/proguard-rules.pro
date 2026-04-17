# 애노테이션 보존 (Room, Compose)
-keepattributes *Annotation*, Signature, InnerClasses

# 포커스온 서비스/Receiver — AndroidManifest에서 이름으로 참조
-keep class com.focuson.app.service.** { *; }

# Room 엔티티/DAO는 R8이 리플렉션으로 접근 — 이름 유지
-keep class com.focuson.app.data.db.entity.** { *; }
-keep class com.focuson.app.data.db.** { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keep @androidx.room.Database class * { *; }

# Kotlin 코루틴
-keepclassmembernames class kotlinx.** { volatile <fields>; }

# DataStore
-keep class androidx.datastore.*.** { *; }

# Compose — 대체로 R8 기본 규칙이 처리
# Parcelable 자동 생성 막지 말기
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# 디버그 로그 스트립 (release)
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
}
