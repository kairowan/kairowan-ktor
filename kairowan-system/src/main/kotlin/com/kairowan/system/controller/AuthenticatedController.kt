package com.kairowan.system.controller

import com.kairowan.common.KResult
import com.kairowan.core.controller.KController
import com.kairowan.system.vo.UserInfo
import com.kairowan.system.vo.UserInfoResult
import com.kairowan.core.framework.security.getLoginUser
import com.kairowan.system.api.SystemApiRoutes
import com.kairowan.system.domain.SysUser
import com.kairowan.system.service.SysLoginService
import com.kairowan.system.service.SysMenuService
import io.github.smiley4.ktorswaggerui.dsl.get
import io.github.smiley4.ktorswaggerui.dsl.post
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import com.kairowan.core.controller.AuthenticatedRouteController

/**
 * 需要认证的路由
 */
class AuthenticatedController : KController(), AuthenticatedRouteController {

    private val logger = LoggerFactory.getLogger(AuthenticatedController::class.java)

    override fun register(route: Route) {
        route.routes()
    }

    private fun Route.routes() {
        val loginService by inject<SysLoginService>()
        val menuService by inject<SysMenuService>()

        route(SystemApiRoutes.Auth.ROOT) {
            // 登出接口
            post(SystemApiRoutes.Auth.LOGOUT, {
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
            get(SystemApiRoutes.Auth.GET_INFO, {
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

                val sysUser = loginUser.user as? SysUser
                val userInfo = UserInfo(
                    userId = loginUser.userId.toLong(),
                    userName = loginUser.username,
                    nickName = sysUser?.nickName ?: "",
                    email = sysUser?.email ?: "",
                    phone = sysUser?.phone ?: "",
                    deptId = sysUser?.deptId?.toLong()
                )

                call.respond(KResult.ok(UserInfoResult(
                    user = userInfo,
                    roles = loginUser.roles,
                    permissions = loginUser.permissions
                )))
            }

            // 获取路由菜单
            get(SystemApiRoutes.Auth.GET_ROUTERS, {
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

                try {
                    val routers = menuService.selectMenuTreeByUserId(loginUser.userId)
                    call.respond(KResult.ok(routers))
                } catch (e: Exception) {
                    logger.error("Failed to get routers for userId=${loginUser.userId}", e)
                    // 返回空列表，避免阻塞登录
                    call.respond(KResult.ok(emptyList<Any>()))
                }
            }
        }
    }
}
