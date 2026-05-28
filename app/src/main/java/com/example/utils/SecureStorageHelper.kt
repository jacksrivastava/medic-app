package com.example.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

object SecureStorageHelper {
    private const val TAG = "SecureStorageHelper"
    private const val SECURE_DIR_NAME = "secure_medical_documents"

    /**
     * Gets a reference to the secure app-private documents directory.
     */
    private fun getSecureDir(context: Context): File {
        val dir = File(context.filesDir, SECURE_DIR_NAME)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Extracts the real file name from a content Uri.
     */
    fun getFileName(context: Context, uri: Uri): String {
        var name = "unknown_document"
        try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        name = it.getString(index)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to resolve file name from Uri, using fallback", e)
            uri.lastPathSegment?.let { name = it }
        }
        return name
    }

    /**
     * Reads a file from Uri, encrypts its binary bytes, and saves it in app's private files folder.
     * Returns the absolute path of the encrypted file on success, or null on error.
     */
    fun encryptAndSaveFile(context: Context, uri: Uri): String? {
        return try {
            val inputStream: InputStream = context.contentResolver.openInputStream(uri) 
                ?: throw IllegalArgumentException("Could not open input stream from Uri")
            val rawBytes = inputStream.readBytes()
            inputStream.close()
            
            // Encrypt raw binary data
            val encryptedBytes = CryptoManager.encrypt(rawBytes)
            
            // Generate secure unique storage file name
            val originalName = getFileName(context, uri).replace("\\s+".toRegex(), "_")
            val secureFile = File(getSecureDir(context), "enc_${System.currentTimeMillis()}_$originalName")
            
            // Write to private storage
            FileOutputStream(secureFile).use { outputStream ->
                outputStream.write(encryptedBytes)
                outputStream.flush()
            }
            
            Log.d(TAG, "File successfully encrypted and stored at: ${secureFile.absolutePath}")
            secureFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed encryption file copy process", e)
            null
        }
    }

    /**
     * Reads the encrypted file at [filePath], decrypts it, and returns the raw decrypted bytes.
     */
    fun decryptAndReadFile(filePath: String): ByteArray? {
        val file = File(filePath)
        if (!file.exists()) {
            Log.e(TAG, "Target encrypted file does not exist: $filePath")
            return null
        }
        return try {
            val encryptedBytes = FileInputStream(file).use { it.readBytes() }
            val decryptedBytes = CryptoManager.decrypt(encryptedBytes)
            decryptedBytes
        } catch (e: Exception) {
            Log.e(TAG, "Decryption read process failed on file $filePath", e)
            null
        }
    }

    /**
     * Deletes a securely stored file if no longer needed.
     */
    fun deleteSecureFile(filePath: String): Boolean {
        val file = File(filePath)
        return if (file.exists()) {
            val result = file.delete()
            Log.d(TAG, "File deleted ($filePath): $result")
            result
        } else {
            false
        }
    }
}
