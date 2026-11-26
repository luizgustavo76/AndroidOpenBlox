plugins {
    id("com.android.application")
}

android {
    namespace = "com.openblox.android"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.openblox.android"
        minSdk = 9
        targetSdk = 19
        versionCode = 1
        versionName = "1.0"

        ndk {
            abiFilters += listOf("armeabi")
        }
    }

    signingConfigs {
        getByName("debug") {
            enableV1Signing = true
            enableV2Signing = true
        }
    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
