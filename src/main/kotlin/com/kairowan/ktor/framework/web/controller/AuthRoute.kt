package com.kairowan.ktor.framework.web.controller

import com.kairowan.ktor.common.KResult
import com.kairowan.ktor.framework.security.getLoginUser
import com.kairowan.ktor.framework.web.dto.*
import com.kairowan.ktor.framework.web.service.SysLoginService
import com.kairowan.ktor.framework.web.service.SysMenuService
import com.kairowan.ktor.framework.web.service.SysPermissionService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import io.github.smiley4.ktorswaggerui.dsl.post
import io.github.smiley4.ktorswaggerui.dsl.get
import io.ktor.http.*

/**
 * 认证控制器
 * 处理登录、登出、获取用户信息等
 *
 * @author Kairowan
 * @date 2026-01-18
 */
class AuthController : KController() {

    fun Route.routes() {
        val loginService by inject<SysLoginService>()
        val menuService by inject<SysMenuService>()

        // 登录接口 (公开)
        post("/login", {
            tags = listOf("Auth")
            summary = "用户登录"
            description = "使用用户名密码登录，返回JWT Token"
            request {
                body<LoginBody> {
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
            val loginBody = call.receive<LoginBody>()
            val token = loginService.login(loginBody)
            call.respond(KResult.ok(LoginResult(token)))
        }
    }
}

/**
 * 需要认证的路由
 */
class AuthenticatedController : KController() {

    fun Route.routes() {
        val loginService by inject<SysLoginService>()
        val menuService by inject<SysMenuService>()
        val permissionService by inject<SysPermissionService>()

        // 登出接口
        post("/logout", {
            tags = listOf("Auth")
            summary = "用户登出"
            description = "登出当前用户，Token将被加入黑名单"
            securitySchemeName = "BearerAuth"
        }) {
            val token = call.request.header(HttpHeaders.Authorization) ?: ""
            loginService.logout(token)
            call.respond(KResult.ok<Any>(msg = "登出成功"))
        }

        // 获取用户信息
        get("/getInfo", {
            tags = listOf("Auth")
            summary = "获取用户信息"
            description = "获取当前登录用户的基本信息、角色和权限"
            securitySchemeName = "BearerAuth"
            response {
                HttpStatusCode.OK to {
                    description = "用户信息"
                    body<KResult<UserInfoResult>>()
                }
            }
        }) {
            val loginUser = call.getLoginUser()
            if (loginUser == null) {
                call.respond(KResult.fail<Any>("用户未登录"))
                return@get
            }
            
            val sysUser = loginUser.user
            val userInfo = UserInfo(
                userId = loginUser.userId,
                userName = loginUser.username,
                nickName = sysUser?.nickName ?: "",
                email = sysUser?.email ?: "",
                phone = sysUser?.phone ?: "",
                deptId = sysUser?.deptId
            )
            
            call.respond(KResult.ok(UserInfoResult(
                user = userInfo,
                roles = loginUser.roles,
                permissions = loginUser.permissions
            )))
        }

        // 获取路由菜单
        get("/getRouters", {
            tags = listOf("Auth")
            summary = "获取路由菜单"
            description = "获取当前用户的动态路由菜单（用于前端）"
            securitySchemeName = "BearerAuth"
        }) {
            val loginUser = call.getLoginUser()
            if (loginUser == null) {
                call.respond(KResult.fail<Any>("用户未登录"))
                return@get
            }
            
            val routers = menuService.selectMenuTreeByUserId(loginUser.userId)
            call.respond(KResult.ok(routers))
        }
    }
}

fun Route.authRoutes() {
    AuthController().apply { routes() }
}

fun Route.authenticatedAuthRoutes() {
    AuthenticatedController().apply { routes() }
}
