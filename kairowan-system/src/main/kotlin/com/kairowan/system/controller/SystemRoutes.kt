package com.kairowan.system.controller

import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import com.kairowan.core.controller.PublicRouteController
import com.kairowan.core.controller.AuthenticatedRouteController

/**
 * 系统模块路由注册（公开路由）
 */
fun Route.systemRoutes() {
    val controllers by inject<List<PublicRouteController>>()
    controllers.forEach { it.register(this) }
}

/**
 * 系统模块路由注册（需要认证的路由）
 */
fun Route.authenticatedSystemRoutes() {
    // 需要认证的路由
    authenticate {
        val controllers by inject<List<AuthenticatedRouteController>>()
        controllers.forEach { it.register(this) }
    }
}
