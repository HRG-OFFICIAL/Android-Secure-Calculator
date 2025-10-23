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

// Custom Gradle tasks for obfuscation and security
tasks.register("extractCertFingerprint") {
    group = "security"
    description = "Extract certificate fingerprint and generate CertificateInfo.kt"
    
    doLast {
        val keystorePath = android.signingConfigs.getByName("debug").storeFile
        val keystorePassword = android.signingConfigs.getByName("debug").storePassword ?: "android"
        
        if (keystorePath != null && keystorePath.exists()) {
            try {
                val process = ProcessBuilder(
                    "keytool", "-list", "-v", "-keystore", keystorePath.absolutePath,
                    "-alias", "androiddebugkey", "-storepass", keystorePassword
                ).start()
                
                val output = process.inputStream.bufferedReader().readText()
                val error = process.errorStream.bufferedReader().readText()
                
                if (process.waitFor() == 0) {
                    // Extract SHA-256 fingerprint
                    val sha256Pattern = Regex("SHA256:\\s*([A-F0-9:]+)")
                    val match = sha256Pattern.find(output)
                    
                    if (match != null) {
                        val fingerprint = match.groupValues[1]
                        println("Extracted certificate fingerprint: $fingerprint")
                        
                        // Update CertificateInfo.kt
                        val certInfoFile = file("src/main/java/com/android/calculator/security/CertificateInfo.kt")
                        if (certInfoFile.exists()) {
                            val content = certInfoFile.readText()
                            val updatedContent = content.replace(
                                "SHA256: 14:6D:E9:83:C5:73:17:34:02:85:12:8F:32:37:4E:85:D3:ED:F3:AA:8C:0A:BC:10:24:02:1C:60:5D:BE:AB:A6",
                                "SHA256: $fingerprint"
                            )
                            certInfoFile.writeText(updatedContent)
                            println("Updated CertificateInfo.kt with fingerprint")
                        }
                    } else {
                        println("Could not extract SHA-256 fingerprint from keytool output")
                    }
                } else {
                    println("keytool failed: $error")
                }
            } catch (e: Exception) {
                println("Failed to extract certificate fingerprint: ${e.message}")
            }
        } else {
            println("Debug keystore not found, skipping fingerprint extraction")
        }
    }
}

tasks.register("encryptAssets") {
    group = "security"
    description = "Encrypt assets at build time"
    
    doLast {
        val assetsDir = file("src/main/assets")
        val secureConfigFile = file("src/main/assets/secure_config.json")
        
        if (!assetsDir.exists()) {
            assetsDir.mkdirs()
        }
        
        // Create sample secure config if it doesn't exist
        if (!secureConfigFile.exists()) {
            val sampleConfig = """
                {
                    "api_endpoint": "https://api.secure-calculator.com/v1",
                    "encryption_key": "secure_key_2025",
                    "feature_flags": {
                        "advanced_crypto": true,
                        "cloud_sync": false
                    }
                }
            """.trimIndent()
            secureConfigFile.writeText(sampleConfig)
            println("Created sample secure_config.json")
        }
        
        // Encrypt the config file
        try {
            val originalContent = secureConfigFile.readText()
            val encryptedContent = encryptString(originalContent)
            
            val encryptedFile = file("src/main/assets/secure_config.json.enc")
            encryptedFile.writeText(encryptedContent)
            
            println("Encrypted secure_config.json -> secure_config.json.enc")
        } catch (e: Exception) {
            println("Failed to encrypt assets: ${e.message}")
        }
    }
}

// Helper function for asset encryption
fun encryptString(plaintext: String): String {
    return try {
        val key = "SecureAssetKey2025!".toByteArray().sliceArray(0..15)
        val cipher = javax.crypto.Cipher.getInstance("AES/ECB/PKCS5Padding")
        val secretKey = javax.crypto.spec.SecretKeySpec(key, "AES")
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, secretKey)
        val encrypted = cipher.doFinal(plaintext.toByteArray())
        // Simple base64 encoding for Gradle context
        encrypted.encodeBase64()
    } catch (e: Exception) {
        println("Encryption failed: ${e.message}")
        plaintext
    }
}

// Simple base64 encoding function for Gradle
fun ByteArray.encodeBase64(): String {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
    val result = StringBuilder()
    var i = 0
    while (i < this.size) {
        val b1 = this[i++].toInt() and 0xFF
        val b2 = if (i < this.size) this[i++].toInt() and 0xFF else 0
        val b3 = if (i < this.size) this[i++].toInt() and 0xFF else 0
        
        val bitmap = (b1 shl 16) or (b2 shl 8) or b3
        
        result.append(chars[(bitmap shr 18) and 63])
        result.append(chars[(bitmap shr 12) and 63])
        result.append(if (i - 2 < this.size) chars[(bitmap shr 6) and 63] else '=')
        result.append(if (i - 1 < this.size) chars[bitmap and 63] else '=')
    }
    return result.toString()
}

// Wire tasks into build flow
tasks.named("preBuild") {
    dependsOn("extractCertFingerprint", "encryptAssets")
}
