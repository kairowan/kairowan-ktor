package com.kairowan.ktor.framework.web.controller

import com.kairowan.ktor.common.KResult
import com.kairowan.ktor.framework.web.service.CaptchaService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import io.github.smiley4.ktorswaggerui.dsl.get

/**
 * 验证码控制器
 * @author Kairowan
 * @date 2026-01-18
 */
fun Route.captchaRoutes() {
    val captchaService by inject<CaptchaService>()

    // 获取验证码 (公开接口，无需认证)
    get("/captchaImage", {
        tags = listOf("Captcha")
        summary = "获取验证码图片"
        description = "返回 UUID 和 Base64 编码的验证码图片"
    }) {
        val result = captchaService.createCaptcha()
        call.respond(KResult.ok(mapOf(
            "uuid" to result.uuid,
            "img" to "data:image/png;base64,${result.img}"
        )))
    }
}
