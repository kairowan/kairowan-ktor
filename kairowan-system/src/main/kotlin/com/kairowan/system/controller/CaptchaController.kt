package com.kairowan.system.controller

import com.kairowan.common.KResult
import com.kairowan.core.controller.KController
import com.kairowan.system.api.SystemApiRoutes
import com.kairowan.system.service.CaptchaService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import io.github.smiley4.ktorswaggerui.dsl.get
import com.kairowan.core.controller.PublicRouteController

/**
 * 验证码控制器
 * @author Kairowan
 * @date 2026-01-18
 */
class CaptchaController : KController(), PublicRouteController {
    override fun register(route: Route) {
        route.routes()
    }

    private fun Route.routes() {
        val captchaService by inject<CaptchaService>()

        // 获取验证码 (公开接口，无需认证)
        get(SystemApiRoutes.Captcha.IMAGE, {
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
}
