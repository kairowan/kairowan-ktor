package com.kairowan.ktor.common.utils

import org.mindrot.jbcrypt.BCrypt
import java.util.Base64
import javax.crypto.Cipher
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
    
    fun encryptAES(content: String): String {
        val key = SecretKeySpec(AES_KEY.toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encrypted = cipher.doFinal(content.toByteArray())
        return Base64.getEncoder().encodeToString(encrypted)
    }
    
    fun decryptAES(content: String): String {
        val key = SecretKeySpec(AES_KEY.toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, key)
        val decoded = Base64.getDecoder().decode(content)
        val original = cipher.doFinal(decoded)
        return String(original)
    }
}
