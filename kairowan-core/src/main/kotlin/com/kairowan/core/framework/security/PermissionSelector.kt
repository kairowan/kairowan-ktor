package com.kairowan.core.framework.security

import io.ktor.server.routing.*

/**
 * 权限选择器 (用于路由匹配)
 */
internal class PermissionSelector(private val permission: String) : RouteSelector() {
    override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation {
        return RouteSelectorEvaluation.Transparent
    }

    override fun toString(): String = "(permission:$permission)"
}
