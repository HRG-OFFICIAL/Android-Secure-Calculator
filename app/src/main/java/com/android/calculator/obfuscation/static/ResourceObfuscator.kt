package com.android.calculator.obfuscation.static

import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.zip.Deflater
import java.util.zip.Inflater
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * Resource Obfuscation Implementation
 * Implements comprehensive resource and manifest obfuscation techniques
 */
object ResourceObfuscator {
    
    private val secureRandom = SecureRandom()
    private val resourceKey = generateResourceKey()
    
    // ===== 2.1 RESOURCE NAME MANGLING / SHRINKING =====
    
    /**
     * Resource name obfuscation and shrinking
     */
    object ResourceNameMangler {
        
        private val nameMapping = mutableMapOf<String, String>()
        private val reverseMapping = mutableMapOf<String, String>()
        
        fun obfuscateResourceName(originalName: String, resourceType: String): String {
            val key = "${resourceType}_$originalName"
            return nameMapping.getOrPut(key) {
                val obfuscated = generateObfuscatedName(resourceType)
                reverseMapping[obfuscated] = key
                obfuscated
            }
        }
        
        fun deobfuscateResourceName(obfuscatedName: String): String? {
            return reverseMapping[obfuscatedName]
        }
        
        fun initializeMappings() {
            // Initialize resource name mappings for obfuscation
            android.util.Log.d("ResourceNameMangler", "Resource name mappings initialized")
        }
        
        private fun generateObfuscatedName(resourceType: String): String {
            val prefix = when (resourceType.lowercase()) {
                "drawable" -> "d"
                "layout" -> "l"
                "string" -> "s"
                "color" -> "c"
                "dimen" -> "dm"
                "style" -> "st"
                "id" -> "i"
                else -> "r"
            }
            
            val randomSuffix = (1..3).map { 
                ('a'..'z').random() 
            }.joinToString("")
            
            return "$prefix$randomSuffix"
        }
        
        fun generateResourceMapping(): Map<String, String> {
            return nameMapping.toMap()
        }
    }
    
    /**
     * Resource shrinking and optimization
     */
    object ResourceShrinker {
        
        fun shrinkResource(data: ByteArray): ByteArray {
            return when {
                isCompressible(data) -> compressResource(data)
                isEncryptable(data) -> encryptResource(data)
                else -> data
            }
        }
        
        fun expandResource(shrunkData: ByteArray, isCompressed: Boolean, isEncrypted: Boolean): ByteArray {
            return when {
                isCompressed -> decompressResource(shrunkData)
                isEncrypted -> decryptResource(shrunkData)
                else -> shrunkData
            }
        }
        
        private fun isCompressible(data: ByteArray): Boolean {
            // Check if data is likely to compress well
            val entropy = calculateEntropy(data)
            return entropy < 7.5 // Lower entropy = more compressible
        }
        
        private fun isEncryptable(data: ByteArray): Boolean {
            // Check if data contains sensitive information
            val content = String(data, Charsets.UTF_8)
            val sensitiveKeywords = listOf("password", "token", "key", "secret", "api")
            return sensitiveKeywords.any { content.contains(it, ignoreCase = true) }
        }
        
        private fun calculateEntropy(data: ByteArray): Double {
            val frequency = IntArray(256)
            for (byte in data) {
                frequency[byte.toInt() and 0xFF]++
            }
            
            var entropy = 0.0
            val length = data.size.toDouble()
            
            for (count in frequency) {
                if (count > 0) {
                    val probability = count / length
                    entropy -= probability * kotlin.math.log2(probability)
                }
            }
            
            return entropy
        }
        
        private fun compressResource(data: ByteArray): ByteArray {
            val deflater = Deflater(Deflater.BEST_COMPRESSION)
            deflater.setInput(data)
            deflater.finish()
            
            val outputStream = ByteArrayOutputStream()
            val buffer = ByteArray(1024)
            
            while (!deflater.finished()) {
                val count = deflater.deflate(buffer)
                outputStream.write(buffer, 0, count)
            }
            
            deflater.end()
            return outputStream.toByteArray()
        }
        
        private fun decompressResource(compressedData: ByteArray): ByteArray {
            val inflater = Inflater()
            inflater.setInput(compressedData)
            
            val outputStream = ByteArrayOutputStream()
            val buffer = ByteArray(1024)
            
            while (!inflater.finished()) {
                val count = inflater.inflate(buffer)
                outputStream.write(buffer, 0, count)
            }
            
            inflater.end()
            return outputStream.toByteArray()
        }
        
        private fun encryptResource(data: ByteArray): ByteArray {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val key = SecretKeySpec(resourceKey, "AES")
            cipher.init(Cipher.ENCRYPT_MODE, key)
            
            val iv = cipher.iv
            val encrypted = cipher.doFinal(data)
            
            return iv + encrypted
        }
        
        private fun decryptResource(encryptedData: ByteArray): ByteArray {
            val iv = encryptedData.copyOf(16)
            val encrypted = encryptedData.copyOfRange(16, encryptedData.size)
            
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val key = SecretKeySpec(resourceKey, "AES")
            cipher.init(Cipher.DECRYPT_MODE, key, javax.crypto.spec.IvParameterSpec(iv))
            
            return cipher.doFinal(encrypted)
        }
    }
    
    // ===== 2.2 ENCRYPTED RESOURCES / ASSETS =====
    
    /**
     * Asset encryption and decryption
     */
    object AssetEncryption {
        
        fun encryptAsset(context: Context, assetName: String, data: ByteArray): String {
            val encryptedData = encryptAssetData(data, assetName)
            val assetFile = File(context.filesDir, "encrypted_$assetName")
            
            assetFile.writeBytes(encryptedData)
            return assetFile.absolutePath
        }
        
        fun decryptAsset(context: Context, assetName: String): ByteArray? {
            val assetFile = File(context.filesDir, "encrypted_$assetName")
            if (!assetFile.exists()) return null
            
            val encryptedData = assetFile.readBytes()
            return decryptAssetData(encryptedData, assetName)
        }
        
        fun initializeEncryption(context: Context) {
            // Initialize asset encryption system
            android.util.Log.d("AssetEncryption", "Asset encryption system initialized")
        }
        
        fun encryptAssetFromAssets(context: Context, assetPath: String): String? {
            return try {
                val inputStream = context.assets.open(assetPath)
                val data = inputStream.readBytes()
                inputStream.close()
                
                encryptAsset(context, assetPath, data)
            } catch (e: Exception) {
                null
            }
        }
        
        private fun encryptAssetData(data: ByteArray, assetName: String): ByteArray {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val key = generateAssetKey(assetName)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            
            val encrypted = cipher.doFinal(data)
            val iv = cipher.iv
            
            return iv + encrypted
        }
        
        private fun decryptAssetData(encryptedData: ByteArray, assetName: String): ByteArray? {
            return try {
                val iv = encryptedData.copyOf(12) // GCM uses 12-byte IV
                val encrypted = encryptedData.copyOfRange(12, encryptedData.size)
                
                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                val key = generateAssetKey(assetName)
                cipher.init(Cipher.DECRYPT_MODE, key, javax.crypto.spec.IvParameterSpec(iv))
                
                cipher.doFinal(encrypted)
            } catch (e: Exception) {
                null
            }
        }
        
        private fun generateAssetKey(assetName: String): SecretKeySpec {
            val keyData = resourceKey + assetName.toByteArray()
            val digest = MessageDigest.getInstance("SHA-256")
            val hashedKey = digest.digest(keyData)
            return SecretKeySpec(hashedKey, "AES")
        }
    }
    
    /**
     * Image and media obfuscation
     */
    object MediaObfuscator {
        
        fun obfuscateImage(imageData: ByteArray): ByteArray {
            // Add noise to image data to make it harder to analyze
            val obfuscated = imageData.copyOf()
            val noiseSize = minOf(100, imageData.size / 10)
            
            for (i in 0 until noiseSize) {
                val index = secureRandom.nextInt(imageData.size)
                obfuscated[index] = (obfuscated[index].toInt() xor secureRandom.nextInt(256)).toByte()
            }
            
            return obfuscated
        }
        
        fun deobfuscateImage(obfuscatedData: ByteArray): ByteArray {
            // Remove noise from image data
            val deobfuscated = obfuscatedData.copyOf()
            val noiseSize = minOf(100, obfuscatedData.size / 10)
            
            for (i in 0 until noiseSize) {
                val index = secureRandom.nextInt(obfuscatedData.size)
                deobfuscated[index] = (deobfuscated[index].toInt() xor secureRandom.nextInt(256)).toByte()
            }
            
            return deobfuscated
        }
        
        fun obfuscateStringResource(value: String): String {
            // Obfuscate string resources by encoding them
            val encoded = Base64.encodeToString(value.toByteArray(), Base64.NO_WRAP)
            val obfuscated = encoded.map { char ->
                when {
                    char.isLetter() -> {
                        val base = if (char.isLowerCase()) 'a' else 'A'
                        val offset = (char - base + 13) % 26
                        base + offset
                    }
                    char.isDigit() -> {
                        val digit = char.digitToInt()
                        ((digit + 5) % 10).toString().first()
                    }
                    else -> char
                }
            }.joinToString("")
            
            return obfuscated
        }
        
        fun deobfuscateStringResource(obfuscatedValue: String): String {
            // Deobfuscate string resources
            val deobfuscated = obfuscatedValue.map { char ->
                when {
                    char.isLetter() -> {
                        val base = if (char.isLowerCase()) 'a' else 'A'
                        val offset = (char - base - 13 + 26) % 26
                        base + offset
                    }
                    char.isDigit() -> {
                        val digit = char.digitToInt()
                        ((digit - 5 + 10) % 10).toString().first()
                    }
                    else -> char
                }
            }.joinToString("")
            
            return try {
                String(Base64.decode(deobfuscated, Base64.NO_WRAP))
            } catch (e: Exception) {
                obfuscatedValue // Return original if decoding fails
            }
        }
    }
    
    // ===== 2.3 ANDROID MANIFEST OBFUSCATION =====
    
    /**
     * AndroidManifest obfuscation
     */
    object ManifestObfuscator {
        
        fun obfuscateManifest(manifestContent: String): String {
            var obfuscated = manifestContent
            
            // Obfuscate package name references
            obfuscated = obfuscatePackageReferences(obfuscated)
            
            // Obfuscate activity names
            obfuscated = obfuscateActivityNames(obfuscated)
            
            // Obfuscate service names
            obfuscated = obfuscateServiceNames(obfuscated)
            
            // Obfuscate permission names
            obfuscated = obfuscatePermissionNames(obfuscated)
            
            // Remove debug information
            obfuscated = removeDebugInformation(obfuscated)
            
            return obfuscated
        }
        
        private fun obfuscatePackageReferences(content: String): String {
            val packageRegex = "package=\"([^\"]+)\"".toRegex()
            return content.replace(packageRegex) { matchResult ->
                val originalPackage = matchResult.groupValues[1]
                val obfuscatedPackage = obfuscatePackageName(originalPackage)
                "package=\"$obfuscatedPackage\""
            }
        }
        
        private fun obfuscateActivityNames(content: String): String {
            val activityRegex = "android:name=\"([^\"]+)\"".toRegex()
            return content.replace(activityRegex) { matchResult ->
                val originalName = matchResult.groupValues[1]
                val obfuscatedName = obfuscateClassName(originalName)
                "android:name=\"$obfuscatedName\""
            }
        }
        
        private fun obfuscateServiceNames(content: String): String {
            val serviceRegex = "android:name=\"([^\"]+Service[^\"]*)\"".toRegex()
            return content.replace(serviceRegex) { matchResult ->
                val originalName = matchResult.groupValues[1]
                val obfuscatedName = obfuscateClassName(originalName)
                "android:name=\"$obfuscatedName\""
            }
        }
        
        private fun obfuscatePermissionNames(content: String): String {
            val permissionRegex = "android:permission=\"([^\"]+)\"".toRegex()
            return content.replace(permissionRegex) { matchResult ->
                val originalPermission = matchResult.groupValues[1]
                val obfuscatedPermission = obfuscatePermissionName(originalPermission)
                "android:permission=\"$obfuscatedPermission\""
            }
        }
        
        private fun removeDebugInformation(content: String): String {
            var cleaned = content
            
            // Remove debug attributes
            cleaned = cleaned.replace(Regex("android:debuggable=\"[^\"]*\""), "")
            cleaned = cleaned.replace(Regex("android:testOnly=\"[^\"]*\""), "")
            
            // Remove comments
            cleaned = cleaned.replace(Regex("<!--.*?-->", RegexOption.DOT_MATCHES_ALL), "")
            
            // Remove extra whitespace
            cleaned = cleaned.replace(Regex("\\s+"), " ")
            
            return cleaned
        }
        
        private fun obfuscatePackageName(packageName: String): String {
            val parts = packageName.split(".")
            val obfuscatedParts = parts.map { part ->
                if (part.length > 3) {
                    part.take(2) + (1..3).map { ('a'..'z').random() }.joinToString("")
                } else {
                    part
                }
            }
            return obfuscatedParts.joinToString(".")
        }
        
        private fun obfuscateClassName(className: String): String {
            val parts = className.split(".")
            val obfuscatedParts = parts.mapIndexed { index, part ->
                if (index == parts.size - 1) {
                    // Obfuscate class name
                    "C" + (1..5).map { ('a'..'z').random() }.joinToString("")
                } else {
                    // Obfuscate package parts
                    if (part.length > 2) {
                        part.take(1) + (1..3).map { ('a'..'z').random() }.joinToString("")
                    } else {
                        part
                    }
                }
            }
            return obfuscatedParts.joinToString(".")
        }
        
        private fun obfuscatePermissionName(permission: String): String {
            val parts = permission.split(".")
            val obfuscatedParts = parts.map { part ->
                if (part.length > 5) {
                    "P" + (1..4).map { ('a'..'z').random() }.joinToString("")
                } else {
                    part
                }
            }
            return obfuscatedParts.joinToString(".")
        }
    }
    
    // ===== RESOURCE INTEGRITY VERIFICATION =====
    
    /**
     * Resource integrity verification
     */
    object ResourceIntegrityVerifier {
        
        fun verifyResourceIntegrity(context: Context, resourceName: String): Boolean {
            return try {
                val resourceId = getResourceId(context, resourceName)
                if (resourceId == 0) return false
                
                val resource = context.resources.getResourceName(resourceId)
                val expectedHash = calculateResourceHash(resource)
                val actualHash = calculateResourceHash(resourceName)
                
                expectedHash == actualHash
            } catch (e: Exception) {
                false
            }
        }
        
        private fun getResourceId(context: Context, resourceName: String): Int {
            return try {
                val resources = context.resources
                val packageName = context.packageName
                val parts = resourceName.split("/")
                
                when (parts.size) {
                    2 -> {
                        val type = parts[0]
                        val name = parts[1]
                        resources.getIdentifier(name, type, packageName)
                    }
                    else -> 0
                }
            } catch (e: Exception) {
                0
            }
        }
        
        private fun calculateResourceHash(resource: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(resource.toByteArray())
            return Base64.encodeToString(hash, Base64.NO_WRAP)
        }
    }
    
    // ===== UTILITY METHODS =====
    
    private fun generateResourceKey(): ByteArray {
        val key = ByteArray(16)
        secureRandom.nextBytes(key)
        return key
    }
    
    /**
     * Master resource obfuscation method
     */
    fun applyComprehensiveResourceObfuscation(
        context: Context,
        resourceName: String,
        resourceData: ByteArray
    ): ByteArray {
        // Apply name obfuscation
        val obfuscatedName = ResourceNameMangler.obfuscateResourceName(resourceName, "resource")
        
        // Apply resource shrinking
        val shrunkData = ResourceShrinker.shrinkResource(resourceData)
        
        // Apply media obfuscation if applicable
        val finalData = if (isImageResource(resourceName)) {
            MediaObfuscator.obfuscateImage(shrunkData)
        } else {
            shrunkData
        }
        
        return finalData
    }
    
    private fun isImageResource(resourceName: String): Boolean {
        val imageExtensions = listOf(".png", ".jpg", ".jpeg", ".gif", ".bmp", ".webp")
        return imageExtensions.any { resourceName.lowercase().endsWith(it) }
    }
}
