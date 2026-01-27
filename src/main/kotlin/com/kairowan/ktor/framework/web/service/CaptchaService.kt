package com.kairowan.ktor.framework.web.service

import com.kairowan.ktor.core.cache.CacheProvider
import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO

/**
 * 验证码服务
 * @author Kairowan
 * @date 2026-01-18
 */
class CaptchaService(private val cache: CacheProvider) {

    companion object {
        private const val CAPTCHA_PREFIX = "captcha:"
        private const val CAPTCHA_EXPIRE_SECONDS = 300 // 5分钟
        private const val CAPTCHA_LENGTH = 4
        private const val IMAGE_WIDTH = 120
        private const val IMAGE_HEIGHT = 40
        
        private val CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray()
        private val RANDOM = Random()
    }

    /**
     * 生成验证码
     * @return Pair<UUID, Base64Image>
     */
    fun createCaptcha(): CaptchaResult {
        val uuid = UUID.randomUUID().toString()
        val code = generateCode()
        
        // 存入缓存
        cache.set("$CAPTCHA_PREFIX$uuid", code.lowercase(), CAPTCHA_EXPIRE_SECONDS)
        
        // 生成图片
        val imageBase64 = generateImage(code)
        
        return CaptchaResult(
            uuid = uuid,
            img = imageBase64
        )
    }

    /**
     * 验证验证码
     */
    fun verifyCaptcha(uuid: String, code: String): Boolean {
        if (uuid.isBlank() || code.isBlank()) {
            return false
        }
        
        val key = "$CAPTCHA_PREFIX$uuid"
        val cachedCode = cache.get(key) ?: return false
        
        // 验证后删除，一次性使用
        cache.delete(key)
        
        return cachedCode.equals(code, ignoreCase = true)
    }

    /**
     * 生成随机验证码
     */
    private fun generateCode(): String {
        return (1..CAPTCHA_LENGTH)
            .map { CHARS[RANDOM.nextInt(CHARS.size)] }
            .joinToString("")
    }

    /**
     * 生成验证码图片 (Base64)
     */
    private fun generateImage(code: String): String {
        val image = BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB)
        val g = image.createGraphics()
        
        // 背景
        g.color = Color.WHITE
        g.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT)
        
        // 边框
        g.color = Color.LIGHT_GRAY
        g.drawRect(0, 0, IMAGE_WIDTH - 1, IMAGE_HEIGHT - 1)
        
        // 干扰线
        for (i in 0..5) {
            g.color = randomColor(160, 200)
            val x1 = RANDOM.nextInt(IMAGE_WIDTH)
            val y1 = RANDOM.nextInt(IMAGE_HEIGHT)
            val x2 = RANDOM.nextInt(IMAGE_WIDTH)
            val y2 = RANDOM.nextInt(IMAGE_HEIGHT)
            g.drawLine(x1, y1, x2, y2)
        }
        
        // 绘制字符
        g.font = Font("Arial", Font.BOLD, 28)
        val charWidth = IMAGE_WIDTH / (CAPTCHA_LENGTH + 1)
        
        code.forEachIndexed { index, char ->
            g.color = randomColor(20, 130)
            val x = (index + 1) * charWidth - 10
            val y = IMAGE_HEIGHT - 10 + RANDOM.nextInt(5)
            g.drawString(char.toString(), x, y)
        }
        
        // 噪点
        for (i in 0..30) {
            g.color = randomColor(100, 180)
            val x = RANDOM.nextInt(IMAGE_WIDTH)
            val y = RANDOM.nextInt(IMAGE_HEIGHT)
            g.fillRect(x, y, 2, 2)
        }
        
        g.dispose()
        
        // 转 Base64
        val baos = ByteArrayOutputStream()
        ImageIO.write(image, "png", baos)
        return Base64.getEncoder().encodeToString(baos.toByteArray())
    }

    private fun randomColor(min: Int, max: Int): Color {
        val r = min + RANDOM.nextInt(max - min)
        val g = min + RANDOM.nextInt(max - min)
        val b = min + RANDOM.nextInt(max - min)
        return Color(r, g, b)
    }
}

/**
 * 验证码结果
 */
data class CaptchaResult(
    val uuid: String,
    val img: String  // Base64 encoded image
)
