package com.kairowan.ktor.framework.security

import com.kairowan.ktor.common.KResult
import com.kairowan.ktor.common.constant.ResultCode
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*

/**
 * 权限校验扩展函数
 * @author Kairowan
 * @date 2026-01-18
 */

/**
 * 获取当前登录用户
 */
fun ApplicationCall.getLoginUser(): LoginUser? {
    return principal<LoginUser>()
}

/**
 * 获取当前登录用户ID
 */
fun ApplicationCall.getUserId(): Int {
    return getLoginUser()?.userId ?: throw IllegalStateException("User not authenticated")
}

/**
 * 获取当前登录用户名
 */
fun ApplicationCall.getUsername(): String {
    return getLoginUser()?.username ?: throw IllegalStateException("User not authenticated")
}

/**
 * 检查是否有权限
 */
fun LoginUser.hasPermission(permission: String): Boolean {
    if (permissions.contains("*:*:*")) {
        return true // 超级管理员
    }
    return permissions.contains(permission)
}

/**
 * 检查是否有角色
 */
fun LoginUser.hasRole(role: String): Boolean {
    if (roles.contains("admin")) {
        return true // 超级管理员
    }
    return roles.contains(role)
}

/**
 * 权限校验路由扩展
 * 用法: requirePermission("system:user:add") { ... }
 */
fun Route.requirePermission(permission: String, build: Route.() -> Unit): Route {
    val route = createChild(PermissionSelector(permission))
    route.intercept(ApplicationCallPipeline.Call) {
        val user = call.principal<LoginUser>()
        if (user == null || !user.hasPermission(permission)) {
            call.respond(KResult.fail<Any>(ResultCode.FORBIDDEN))
            finish()
            return@intercept
        }
    }
    route.build()
    return route
}

/**
 * 角色校验路由扩展
 * 用法: requireRole("admin") { ... }
 */
fun Route.requireRole(role: String, build: Route.() -> Unit): Route {
    val route = createChild(RoleSelector(role))
    route.intercept(ApplicationCallPipeline.Call) {
        val user = call.principal<LoginUser>()
        if (user == null || !user.hasRole(role)) {
            call.respond(KResult.fail<Any>(ResultCode.FORBIDDEN))
            finish()
            return@intercept
        }
    }
    route.build()
    return route
}

/**
 * 权限选择器 (用于路由匹配)
 */
private class PermissionSelector(private val permission: String) : RouteSelector() {
    override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation {
        return RouteSelectorEvaluation.Transparent
    }
    override fun toString(): String = "(permission:$permission)"
}

/**
 * 角色选择器 (用于路由匹配)
 */
private class RoleSelector(private val role: String) : RouteSelector() {
    override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation {
        return RouteSelectorEvaluation.Transparent
    }
    override fun toString(): String = "(role:$role)"
}
