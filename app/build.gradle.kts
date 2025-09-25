plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.android.calculator"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.android.calculator"
        minSdk = 21
        targetSdk = 34
        versionCode = 53
        versionName = "3.2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    
    // Modern resource configuration
    androidResources {
        localeFilters += listOf("ar", "az", "be", "bn", "bs", "cs", "de", "el", "es", "fa", "fr", "hi", "hr", "hu", "in", "it", "ja", "kn", "mk", "ml", "nb-rNO", "nl", "or", "pl", "pt-rBR", "ro", "ru", "sat", "sr", "sv", "tr", "uk", "vi", "zh-rCN", "zh-rHK", "zh-rTW")
    }

    buildTypes {
        release {
            // R8/ProGuard enabled for protected builds with aggressive obfuscation
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
            // For Android Studio compatibility: disable minification in debug
            // The product flavors will handle obfuscation when needed
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }
    
    // Custom build variants for different obfuscation levels
    flavorDimensions += "obfuscation"
    productFlavors {
        create("standard") {
            dimension = "obfuscation"
            isDefault = true  // Make standard the default flavor for Android Studio
            // Uses standard proguard-rules.pro
        }
        create("aggressive") {
            dimension = "obfuscation"
            applicationIdSuffix = ".aggressive"
            // Uses COMPREHENSIVE OBFUSCATION - maximum obfuscation with all techniques
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-comprehensive.pro"
            )
        }
    }

    viewBinding {
        enable = true
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    // 16KB page size compatibility for Android 15+ devices
    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    // AntiDebugSDK dependency added only during protected builds
    implementation(project(":anti-debug-sdk"))
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.material)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidslidinguppanel)
    implementation(libs.androidx.preference.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.gson)
}
