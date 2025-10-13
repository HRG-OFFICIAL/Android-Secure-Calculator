package com.android.calculator.obfuscation.data

import android.util.Base64
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * Data Masking Implementation
 * Implements comprehensive data masking techniques for privacy protection
 */
object DataMasking {
    
    private val secureRandom = SecureRandom()
    private val maskingKey = generateMaskingKey()
    
    // ===== STATIC DATA MASKING (SDM) =====
    
    /**
     * 1. Substitution - Replace sensitive data with predefined plausible values
     */
    object SubstitutionMasking {
        
        private val fakeNames = listOf(
            "John Smith", "Jane Doe", "Bob Johnson", "Alice Brown", "Charlie Wilson",
            "Diana Davis", "Eve Miller", "Frank Garcia", "Grace Lee", "Henry Taylor"
        )
        
        private val fakeEmails = listOf(
            "john@example.com", "jane@test.com", "bob@demo.com", "alice@sample.com",
            "charlie@mock.com", "diana@fake.com", "eve@dummy.com", "frank@placeholder.com"
        )
        
        private val fakePhones = listOf(
            "555-0101", "555-0102", "555-0103", "555-0104", "555-0105",
            "555-0106", "555-0107", "555-0108", "555-0109", "555-0110"
        )
        
        fun maskName(originalName: String): String {
            return fakeNames[originalName.hashCode().mod(fakeNames.size)]
        }
        
        fun maskEmail(originalEmail: String): String {
            return fakeEmails[originalEmail.hashCode().mod(fakeEmails.size)]
        }
        
        fun maskPhone(originalPhone: String): String {
            return fakePhones[originalPhone.hashCode().mod(fakePhones.size)]
        }
        
        fun maskCreditCard(originalCard: String): String {
            val prefix = "4111-1111-1111-"
            val last4 = originalCard.takeLast(4)
            return prefix + last4
        }
        
        fun maskSSN(originalSSN: String): String {
            val last4 = originalSSN.takeLast(4)
            return "XXX-XX-$last4"
        }
    }
    
    /**
     * 2. Shuffling - Randomly reorder data within columns
     */
    object ShufflingMasking {
        
        fun <T> shuffleList(originalList: List<T>): List<T> {
            val shuffled = originalList.toMutableList()
            shuffled.shuffle(secureRandom)
            return shuffled
        }
        
        fun shuffleArray(originalArray: Array<String>): Array<String> {
            val shuffled = originalArray.copyOf()
            shuffled.shuffle()
            return shuffled
        }
        
        fun shuffleMapValues(originalMap: Map<String, String>): Map<String, String> {
            val values = originalMap.values.toList()
            val shuffledValues = shuffleList(values)
            val keys = originalMap.keys.toList()
            
            return keys.zip(shuffledValues).toMap()
        }
    }
    
    /**
     * 3. Redaction - Replace sensitive information with generic characters
     */
    object RedactionMasking {
        
        fun redactString(original: String, visibleChars: Int = 4): String {
            if (original.length <= visibleChars) {
                return "*".repeat(original.length)
            }
            
            val visible = original.takeLast(visibleChars)
            val redacted = "*".repeat(original.length - visibleChars)
            return redacted + visible
        }
        
        fun redactEmail(email: String): String {
            val parts = email.split("@")
            if (parts.size != 2) return "***@***"
            
            val username = redactString(parts[0], 2)
            val domain = redactString(parts[1], 3)
            return "$username@$domain"
        }
        
        fun redactPhone(phone: String): String {
            val digits = phone.filter { it.isDigit() }
            if (digits.length < 4) return "***-***-****"
            
            val last4 = digits.takeLast(4)
            return "***-***-$last4"
        }
        
        fun redactCreditCard(cardNumber: String): String {
            val digits = cardNumber.filter { it.isDigit() }
            if (digits.length < 4) return "****-****-****-****"
            
            val last4 = digits.takeLast(4)
            return "****-****-****-$last4"
        }
        
        fun redactSSN(ssn: String): String {
            val digits = ssn.filter { it.isDigit() }
            if (digits.length < 4) return "XXX-XX-XXXX"
            
            val last4 = digits.takeLast(4)
            return "XXX-XX-$last4"
        }
    }
    
    /**
     * 4. Encryption - Encrypt specific data fields
     */
    object EncryptionMasking {
        
        fun encryptField(data: String, fieldType: String = "default"): String {
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            val key = generateFieldKey(fieldType)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            
            val encrypted = cipher.doFinal(data.toByteArray())
            return Base64.encodeToString(encrypted, Base64.NO_WRAP)
        }
        
        fun decryptField(encryptedData: String, fieldType: String = "default"): String {
            try {
                val encrypted = Base64.decode(encryptedData, Base64.NO_WRAP)
                val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
                val key = generateFieldKey(fieldType)
                cipher.init(Cipher.DECRYPT_MODE, key)
                
                val decrypted = cipher.doFinal(encrypted)
                return String(decrypted)
            } catch (e: Exception) {
                return "DECRYPT_ERROR"
            }
        }
        
        private fun generateFieldKey(fieldType: String): SecretKeySpec {
            val keyData = maskingKey + fieldType.toByteArray()
            val digest = java.security.MessageDigest.getInstance("SHA-256")
            val hashedKey = digest.digest(keyData)
            return SecretKeySpec(hashedKey, "AES")
        }
    }
    
    // ===== DYNAMIC DATA MASKING (DDM) =====
    
    /**
     * Dynamic masking based on user access privileges
     */
    object DynamicMasking {
        
        enum class AccessLevel {
            FULL, PARTIAL, MINIMAL, NONE
        }
        
        fun maskBasedOnAccess(data: String, accessLevel: AccessLevel, dataType: String): String {
            return when (accessLevel) {
                AccessLevel.FULL -> data
                AccessLevel.PARTIAL -> maskPartially(data, dataType)
                AccessLevel.MINIMAL -> maskMinimally(data, dataType)
                AccessLevel.NONE -> maskCompletely(data, dataType)
            }
        }
        
        private fun maskPartially(data: String, dataType: String): String {
            return when (dataType.lowercase()) {
                "email" -> RedactionMasking.redactEmail(data)
                "phone" -> RedactionMasking.redactPhone(data)
                "creditcard" -> RedactionMasking.redactCreditCard(data)
                "ssn" -> RedactionMasking.redactSSN(data)
                else -> RedactionMasking.redactString(data, 4)
            }
        }
        
        private fun maskMinimally(data: String, dataType: String): String {
            return when (dataType.lowercase()) {
                "email" -> "***@***"
                "phone" -> "***-***-****"
                "creditcard" -> "****-****-****-****"
                "ssn" -> "XXX-XX-XXXX"
                else -> "***"
            }
        }
        
        private fun maskCompletely(data: String, dataType: String): String {
            return "***"
        }
    }
    
    // ===== FORMAT-PRESERVING ENCRYPTION (FPE) =====
    
    /**
     * Format-Preserving Encryption that maintains data format
     */
    object FormatPreservingEncryption {
        
        fun encryptPreservingFormat(data: String, format: String): String {
            return when (format.lowercase()) {
                "creditcard" -> encryptCreditCard(data)
                "ssn" -> encryptSSN(data)
                "phone" -> encryptPhone(data)
                "email" -> encryptEmail(data)
                else -> encryptGeneric(data)
            }
        }
        
        fun decryptPreservingFormat(encryptedData: String, format: String): String {
            return when (format.lowercase()) {
                "creditcard" -> decryptCreditCard(encryptedData)
                "ssn" -> decryptSSN(encryptedData)
                "phone" -> decryptPhone(encryptedData)
                "email" -> decryptEmail(encryptedData)
                else -> decryptGeneric(encryptedData)
            }
        }
        
        private fun encryptCreditCard(cardNumber: String): String {
            val digits = cardNumber.filter { it.isDigit() }
            if (digits.length != 16) return cardNumber
            
            val encrypted = encryptNumericString(digits)
            return formatCreditCard(encrypted)
        }
        
        private fun decryptCreditCard(encryptedCard: String): String {
            val digits = encryptedCard.filter { it.isDigit() }
            if (digits.length != 16) return encryptedCard
            
            val decrypted = decryptNumericString(digits)
            return formatCreditCard(decrypted)
        }
        
        private fun encryptSSN(ssn: String): String {
            val digits = ssn.filter { it.isDigit() }
            if (digits.length != 9) return ssn
            
            val encrypted = encryptNumericString(digits)
            return formatSSN(encrypted)
        }
        
        private fun decryptSSN(encryptedSSN: String): String {
            val digits = encryptedSSN.filter { it.isDigit() }
            if (digits.length != 9) return encryptedSSN
            
            val decrypted = decryptNumericString(digits)
            return formatSSN(decrypted)
        }
        
        private fun encryptPhone(phone: String): String {
            val digits = phone.filter { it.isDigit() }
            if (digits.length != 10) return phone
            
            val encrypted = encryptNumericString(digits)
            return formatPhone(encrypted)
        }
        
        private fun decryptPhone(encryptedPhone: String): String {
            val digits = encryptedPhone.filter { it.isDigit() }
            if (digits.length != 10) return encryptedPhone
            
            val decrypted = decryptNumericString(digits)
            return formatPhone(decrypted)
        }
        
        private fun encryptEmail(email: String): String {
            val parts = email.split("@")
            if (parts.size != 2) return email
            
            val encryptedUsername = encryptAlphanumericString(parts[0])
            val encryptedDomain = encryptAlphanumericString(parts[1])
            return "$encryptedUsername@$encryptedDomain"
        }
        
        private fun decryptEmail(encryptedEmail: String): String {
            val parts = encryptedEmail.split("@")
            if (parts.size != 2) return encryptedEmail
            
            val decryptedUsername = decryptAlphanumericString(parts[0])
            val decryptedDomain = decryptAlphanumericString(parts[1])
            return "$decryptedUsername@$decryptedDomain"
        }
        
        private fun encryptGeneric(data: String): String {
            val encrypted = encryptString(data)
            return Base64.encodeToString(encrypted.toByteArray(), Base64.NO_WRAP)
        }
        
        private fun decryptGeneric(encryptedData: String): String {
            val decoded = Base64.decode(encryptedData, Base64.NO_WRAP)
            return decryptString(String(decoded))
        }
        
        private fun encryptNumericString(digits: String): String {
            val result = StringBuilder()
            for (digit in digits) {
                val encryptedDigit = ((digit.digitToInt() + 5) % 10).toString()
                result.append(encryptedDigit)
            }
            return result.toString()
        }
        
        private fun decryptNumericString(encryptedDigits: String): String {
            val result = StringBuilder()
            for (digit in encryptedDigits) {
                val decryptedDigit = ((digit.digitToInt() - 5 + 10) % 10).toString()
                result.append(decryptedDigit)
            }
            return result.toString()
        }
        
        private fun encryptAlphanumericString(text: String): String {
            val result = StringBuilder()
            for (char in text) {
                when {
                    char.isDigit() -> {
                        val encryptedDigit = ((char.digitToInt() + 3) % 10).toString()
                        result.append(encryptedDigit)
                    }
                    char.isLetter() -> {
                        val base = if (char.isLowerCase()) 'a' else 'A'
                        val offset = (char - base + 13) % 26
                        result.append(base + offset)
                    }
                    else -> result.append(char)
                }
            }
            return result.toString()
        }
        
        private fun decryptAlphanumericString(encryptedText: String): String {
            val result = StringBuilder()
            for (char in encryptedText) {
                when {
                    char.isDigit() -> {
                        val decryptedDigit = ((char.digitToInt() - 3 + 10) % 10).toString()
                        result.append(decryptedDigit)
                    }
                    char.isLetter() -> {
                        val base = if (char.isLowerCase()) 'a' else 'A'
                        val offset = (char - base - 13 + 26) % 26
                        result.append(base + offset)
                    }
                    else -> result.append(char)
                }
            }
            return result.toString()
        }
        
        private fun formatCreditCard(digits: String): String {
            return "${digits.substring(0, 4)}-${digits.substring(4, 8)}-${digits.substring(8, 12)}-${digits.substring(12, 16)}"
        }
        
        private fun formatSSN(digits: String): String {
            return "${digits.substring(0, 3)}-${digits.substring(3, 5)}-${digits.substring(5, 9)}"
        }
        
        private fun formatPhone(digits: String): String {
            return "${digits.substring(0, 3)}-${digits.substring(3, 6)}-${digits.substring(6, 10)}"
        }
        
        private fun encryptString(data: String): String {
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            val key = SecretKeySpec(maskingKey, "AES")
            cipher.init(Cipher.ENCRYPT_MODE, key)
            
            val encrypted = cipher.doFinal(data.toByteArray())
            return Base64.encodeToString(encrypted, Base64.NO_WRAP)
        }
        
        private fun decryptString(encryptedData: String): String {
            val encrypted = Base64.decode(encryptedData, Base64.NO_WRAP)
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            val key = SecretKeySpec(maskingKey, "AES")
            cipher.init(Cipher.DECRYPT_MODE, key)
            
            val decrypted = cipher.doFinal(encrypted)
            return String(decrypted)
        }
    }
    
    // ===== NUMERIC DATA MASKING =====
    
    /**
     * Mask numeric values for sensitive calculations
     */
    object NumericMasking {
        
        fun maskNumericValue(value: Double, fieldType: String): String {
            val maskedValue = when (fieldType) {
                "gamma_g" -> value + secureRandom.nextDouble() * 0.001 - 0.0005
                "pi" -> value + secureRandom.nextDouble() * 0.0001 - 0.00005
                "e" -> value + secureRandom.nextDouble() * 0.0001 - 0.00005
                else -> value + secureRandom.nextDouble() * 0.01 - 0.005
            }
            return maskedValue.toString()
        }
        
        fun maskNumericValue(value: Int, fieldType: String): String {
            val maskedValue = when (fieldType) {
                "precision" -> value + secureRandom.nextInt(3) - 1
                "scale" -> value + secureRandom.nextInt(5) - 2
                else -> value + secureRandom.nextInt(10) - 5
            }
            return maskedValue.toString()
        }
        
        fun maskNumericValue(value: Long, fieldType: String): String {
            val maskedValue = when (fieldType) {
                "timestamp" -> value + secureRandom.nextLong() % 1000 - 500
                "id" -> value + secureRandom.nextLong() % 10000 - 5000
                else -> value + secureRandom.nextLong() % 100000 - 50000
            }
            return maskedValue.toString()
        }
    }
    
    // ===== DATA REDACTION =====
    
    /**
     * Selective removal or obscuring of sensitive information
     */
    object DataRedaction {
        
        fun redactSensitiveData(data: String, sensitivePatterns: List<String>): String {
            var redactedData = data
            
            for (pattern in sensitivePatterns) {
                val regex = pattern.toRegex()
                redactedData = redactedData.replace(regex, "***")
            }
            
            return redactedData
        }
        
        fun redactLogData(logMessage: String): String {
            val sensitivePatterns = listOf(
                "password\\s*=\\s*[^\\s]+",
                "token\\s*=\\s*[^\\s]+",
                "key\\s*=\\s*[^\\s]+",
                "secret\\s*=\\s*[^\\s]+",
                "\\b\\d{4}-\\d{4}-\\d{4}-\\d{4}\\b", // Credit card
                "\\b\\d{3}-\\d{2}-\\d{4}\\b", // SSN
                "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b" // Email
            )
            
            return redactSensitiveData(logMessage, sensitivePatterns)
        }
        
        fun redactDisplayData(data: String, maskChar: Char = '*'): String {
            val sensitiveWords = listOf("password", "token", "key", "secret", "ssn", "credit")
            var redactedData = data
            
            for (word in sensitiveWords) {
                val regex = "\\b$word\\b".toRegex(RegexOption.IGNORE_CASE)
                redactedData = redactedData.replace(regex) { matchResult ->
                    matchResult.value.map { maskChar }.joinToString("")
                }
            }
            
            return redactedData
        }
    }
    
    // ===== UTILITY METHODS =====
    
    private fun generateMaskingKey(): ByteArray {
        val key = ByteArray(16)
        secureRandom.nextBytes(key)
        return key
    }
    
    /**
     * Master masking method that applies appropriate technique based on data type
     */
    fun applyAppropriateMasking(data: String, dataType: String, maskingType: String = "redaction"): String {
        return when (maskingType.lowercase()) {
            "substitution" -> when (dataType.lowercase()) {
                "name" -> SubstitutionMasking.maskName(data)
                "email" -> SubstitutionMasking.maskEmail(data)
                "phone" -> SubstitutionMasking.maskPhone(data)
                "creditcard" -> SubstitutionMasking.maskCreditCard(data)
                "ssn" -> SubstitutionMasking.maskSSN(data)
                else -> data
            }
            "redaction" -> when (dataType.lowercase()) {
                "email" -> RedactionMasking.redactEmail(data)
                "phone" -> RedactionMasking.redactPhone(data)
                "creditcard" -> RedactionMasking.redactCreditCard(data)
                "ssn" -> RedactionMasking.redactSSN(data)
                else -> RedactionMasking.redactString(data)
            }
            "encryption" -> EncryptionMasking.encryptField(data, dataType)
            "fpe" -> FormatPreservingEncryption.encryptPreservingFormat(data, dataType)
            else -> data
        }
    }
}
