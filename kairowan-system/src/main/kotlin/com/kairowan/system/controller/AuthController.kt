package com.kairowan.system.controller

import com.kairowan.common.KResult
import com.kairowan.core.controller.KController
import com.kairowan.system.api.SystemApiRoutes
import com.kairowan.core.req.LoginReq
import com.kairowan.system.vo.LoginResult
import com.kairowan.system.service.SysLoginService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import io.github.smiley4.ktorswaggerui.dsl.post
import io.ktor.http.*
import com.kairowan.core.controller.PublicRouteController

/**
 * 认证控制器
 * 处理登录、登出、获取用户信息等
 *
 * @author Kairowan
 * @date 2026-01-18
 */
class AuthController : KController(), PublicRouteController {

    override fun register(route: Route) {
        route.routes()
    }

    private fun Route.routes() {
        val loginService by inject<SysLoginService>()

        route(SystemApiRoutes.Auth.ROOT) {
            // 登录接口 (公开)
            post(SystemApiRoutes.Auth.LOGIN, {
                tags = listOf("Auth")
                summary = "用户登录"
                description = "使用用户名密码登录，返回JWT Token"
                request {
                    body<LoginReq> {
                        description = "登录请求体"
                        required = true
                    }
                }
                response {
                    HttpStatusCode.OK to {
                        description = "登录成功"
                        body<KResult<LoginResult>>()
                    }
                }
            }) {
                val loginBody = call.receive<LoginReq>()
                val token = loginService.login(loginBody.username, loginBody.password)
                call.respond(KResult.ok(LoginResult(token)))
            }
        }
    }
}
