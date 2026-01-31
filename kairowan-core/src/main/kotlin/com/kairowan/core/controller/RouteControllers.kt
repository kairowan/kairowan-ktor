package com.kairowan.core.controller

import io.ktor.server.routing.Route

/**
 * 路由控制器注册接口
 */
interface RouteController {
    fun register(route: Route)
}

/**
 * 公开路由
 */
interface PublicRouteController : RouteController

/**
 * 需要认证的路由
 */
interface AuthenticatedRouteController : RouteController
