import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

val keystoreProps = Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) load(f.inputStream())
}

android {
    namespace = "com.focuson.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.focuson.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 12
        versionName = "0.5.0"

        vectorDrawables { useSupportLibrary = true }
        resourceConfigurations += listOf("ko", "en")

        // 현대 기기용 arm64만 — 32-bit ARM/x86 제외로 APK 크기 30%+ 감소
        ndk {
            abiFilters += listOf("arm64-v8a")
        }

        // ── Pro 라이선스 · 결제 정보 ───────────────────────────
        // keystore.properties 에서 읽음. 누락 시 dev default 사용 (릴리즈 빌드는 반드시 채워야 함)
        buildConfigField(
            "String",
            "LICENSE_SECRET",
            "\"${keystoreProps.getProperty("licenseSecret", "dev-secret-change-me")}\"",
        )
        buildConfigField(
            "String",
            "TOSS_ID",
            "\"${keystoreProps.getProperty("tossId", "yourTossId")}\"",
        )
        buildConfigField(
            "String",
            "PAYMENT_BANK",
            "\"${keystoreProps.getProperty("paymentBank", "토스뱅크")}\"",
        )
        buildConfigField(
            "String",
            "PAYMENT_ACCOUNT",
            "\"${keystoreProps.getProperty("paymentAccount", "0000-0000-0000")}\"",
        )
        buildConfigField(
            "String",
            "PAYMENT_HOLDER",
            "\"${keystoreProps.getProperty("paymentHolder", "개발자")}\"",
        )
        buildConfigField(
            "String",
            "DEV_EMAIL",
            "\"${keystoreProps.getProperty("devEmail", "dev@example.com")}\"",
        )
    }

    // 기본(포커스온) / gyuwon(장규원이 중간고사 대비) 두 에디션
    flavorDimensions += "edition"
    productFlavors {
        create("standard") {
            dimension = "edition"
            // 기본 applicationId 유지: com.focuson.app
        }
        create("gyuwon") {
            dimension = "edition"
            applicationIdSuffix = ".gyuwon"
            versionNameSuffix = "-gyuwon"
        }
    }

    signingConfigs {
        create("release") {
            val storePath = keystoreProps.getProperty("storeFile")
            if (storePath != null) {
                storeFile = rootProject.file(storePath)
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += listOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "/META-INF/*.kotlin_module",
                "/META-INF/DEPENDENCIES",
                "/META-INF/LICENSE*",
                "/META-INF/NOTICE*",
                "/kotlin/**",
                "**/*.kotlin_builtins",
                "**/*.kotlin_metadata",
            )
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)
}
