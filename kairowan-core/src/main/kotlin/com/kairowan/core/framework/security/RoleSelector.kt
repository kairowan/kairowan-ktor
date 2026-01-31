package com.kairowan.core.framework.security

import io.ktor.server.routing.*

/**
 * 角色选择器 (用于路由匹配)
 */
internal class RoleSelector(private val role: String) : RouteSelector() {
    override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation {
        return RouteSelectorEvaluation.Transparent
    }

    override fun toString(): String = "(role:$role)"
}
