package com.kairowan.common.utils

import org.mindrot.jbcrypt.BCrypt
import java.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * 安全工具类 (加密解密)
 * @author Kairowan
 * @date 2026-01-17
 */
object SecurityUtils {
    
    // BCrypt for Password
    fun encryptPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }
    
    fun matchesPassword(raw: String, encoded: String): Boolean {
        return BCrypt.checkpw(raw, encoded)
    }
    
    // AES for Sensitive Data
    private const val AES_KEY = "kairowan-aes-128"
    private const val AES_KEY_ENV = "KAIROWAN_AES_KEY"
    private const val AES_V2_PREFIX = "v2:"
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH_BITS = 128
    
    fun encryptAES(content: String): String {
        val key = SecretKeySpec(resolveAesKey(), "AES")
        val iv = ByteArray(GCM_IV_LENGTH)
        SecureRandom().nextBytes(iv)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
        val encrypted = cipher.doFinal(content.toByteArray())
        val payload = ByteArray(iv.size + encrypted.size)
        System.arraycopy(iv, 0, payload, 0, iv.size)
        System.arraycopy(encrypted, 0, payload, iv.size, encrypted.size)
        return AES_V2_PREFIX + Base64.getEncoder().encodeToString(payload)
    }
    
    fun decryptAES(content: String): String {
        return if (content.startsWith(AES_V2_PREFIX)) {
            val payload = Base64.getDecoder().decode(content.removePrefix(AES_V2_PREFIX))
            if (payload.size <= GCM_IV_LENGTH) {
                ""
            } else {
                val iv = payload.copyOfRange(0, GCM_IV_LENGTH)
                val encrypted = payload.copyOfRange(GCM_IV_LENGTH, payload.size)
                val key = SecretKeySpec(resolveAesKey(), "AES")
                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
                val original = cipher.doFinal(encrypted)
                String(original)
            }
        } else {
            decryptAesEcb(content)
        }
    }

    private fun decryptAesEcb(content: String): String {
        val key = SecretKeySpec(resolveAesKey(), "AES")
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, key)
        val decoded = Base64.getDecoder().decode(content)
        val original = cipher.doFinal(decoded)
        return String(original)
    }

    private fun resolveAesKey(): ByteArray {
        val envKey = System.getenv(AES_KEY_ENV)
        val key = if (envKey != null && isValidAesKeyLength(envKey.length)) {
            envKey
        } else {
            AES_KEY
        }
        return key.toByteArray()
    }

    private fun isValidAesKeyLength(length: Int): Boolean {
        return length == 16 || length == 24 || length == 32
    }
}
