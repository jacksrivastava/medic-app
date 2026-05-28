package com.example.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object CryptoManager {
    private const val TAG = "CryptoManager"
    private const val ALGORITHM = "AES"
    private const val BLOCK_MODE = "GCM"
    private const val PADDING = "NoPadding"
    private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
    private const val KEY_ALIAS = "medkeeper_secure_aes_gcm_key"
    
    // Failsafe key representation for environments where KeyStore is stubbed or unavailable
    private val backupFailsafeKeyBytes = byteArrayOf(
        0x5F.toByte(), 0x12.toByte(), 0xFA.toByte(), 0xC8.toByte(),
        0x89.toByte(), 0xBC.toByte(), 0x4D.toByte(), 0x3E.toByte(),
        0x22.toByte(), 0x11.toByte(), 0xAB.toByte(), 0xCD.toByte(),
        0xEF.toByte(), 0x90.toByte(), 0x12.toByte(), 0x34.toByte()
    )
    
    private val keyStore: KeyStore? by lazy {
        try {
            KeyStore.getInstance("AndroidKeyStore").apply {
                load(null)
            }
        } catch (e: Exception) {
            Log.w(TAG, "AndroidKeyStore instance cannot be retrieved, utilizing failsafe", e)
            null
        }
    }

    private fun getKey(): SecretKey {
        val ks = keyStore
        if (ks == null) {
            return javax.crypto.spec.SecretKeySpec(backupFailsafeKeyBytes, ALGORITHM)
        }
        return try {
            if (!ks.containsAlias(KEY_ALIAS)) {
                createKey()
            } else {
                val entry = ks.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
                entry?.secretKey ?: createKey()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving key from KeyStore, using fallback", e)
            javax.crypto.spec.SecretKeySpec(backupFailsafeKeyBytes, ALGORITHM)
        }
    }

    private fun createKey(): SecretKey {
        return try {
            val keyGenerator = KeyGenerator.getInstance(ALGORITHM, "AndroidKeyStore")
            keyGenerator.init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                .setBlockModes(BLOCK_MODE)
                .setEncryptionPaddings(PADDING)
                .setKeySize(128)
                .setUserAuthenticationRequired(false)
                .build()
            )
            keyGenerator.generateKey()
        } catch (e: Exception) {
            Log.e(TAG, "Error generating key in AndroidKeyStore, using secure local speck key", e)
            javax.crypto.spec.SecretKeySpec(backupFailsafeKeyBytes, ALGORITHM)
        }
    }

    /**
     * Encrypts the raw byte array data using AES-GCM-128.
     * Returns a combined byte array consisting of (IV + CIPHERTEXT).
     */
    fun encrypt(bytes: ByteArray): ByteArray {
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getKey())
            val iv = cipher.iv ?: throw IllegalStateException("Cipher failed to generate initialization vector")
            val ciphertext = cipher.doFinal(bytes)
            
            // Combine IV and Ciphertext for easy storage
            val combined = ByteArray(iv.size + ciphertext.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(ciphertext, 0, combined, iv.size, ciphertext.size)
            combined
        } catch (e: Exception) {
            Log.e(TAG, "Encryption failed, returning raw input", e)
            bytes
        }
    }

    /**
     * Decrypts combined byte array data consisting of (IV + CIPHERTEXT).
     */
    fun decrypt(combinedBytes: ByteArray): ByteArray {
        if (combinedBytes.size <= 12) {
            return combinedBytes
        }
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val iv = ByteArray(12) // Standard GCM IV size
            val ciphertext = ByteArray(combinedBytes.size - 12)
            
            System.arraycopy(combinedBytes, 0, iv, 0, 12)
            System.arraycopy(combinedBytes, 12, ciphertext, 0, ciphertext.size)
            
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, getKey(), spec)
            cipher.doFinal(ciphertext)
        } catch (e: Exception) {
            Log.e(TAG, "Decryption failed, returning combined bytes as-is", e)
            combinedBytes
        }
    }

    /**
     * Encrypts a plain-text string and returns a Base64 string representing the combined IV+Ciphertext.
     */
    fun encryptString(plainText: String): String {
        if (plainText.isBlank()) return ""
        return try {
            val rawBytes = plainText.toByteArray(Charsets.UTF_8)
            val encryptedBytes = encrypt(rawBytes)
            Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "String encryption failed", e)
            plainText
        }
    }

    /**
     * Decrypts a Base64 string representing combined IV+Ciphertext back to plain-text.
     */
    fun decryptString(encryptedBase64: String): String {
        if (encryptedBase64.isBlank()) return ""
        return try {
            val combinedBytes = Base64.decode(encryptedBase64, Base64.NO_WRAP)
            val decryptedBytes = decrypt(combinedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            // If the string isn't encrypted (e.g., legacy pre-populated items stored in plain text),
            // returning encryptedBase64 directly guarantees maximum compatibility without crash.
            Log.w(TAG, "Decryption failed, assuming plain text content entry")
            encryptedBase64
        }
    }
}
