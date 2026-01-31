package com.kairowan.monitor.controller

import com.kairowan.common.KResult
import com.kairowan.core.controller.AuthenticatedRouteController
import com.kairowan.core.controller.KController
import com.kairowan.core.framework.security.requirePermission
import com.kairowan.monitor.api.MonitorApiRoutes
import com.kairowan.monitor.service.DashboardService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

/**
 * 仪表盘控制器
 * @author Kairowan
 * @date 2026-01-29
 */
class DashboardController : KController(), AuthenticatedRouteController {

    override fun register(route: Route) {
        route.routes()
    }

    private fun Route.routes() {
        val dashboardService by inject<DashboardService>()

        route(MonitorApiRoutes.Dashboard.ROOT) {
            requirePermission("monitor:dashboard:view") {
                get(MonitorApiRoutes.Dashboard.STATS) {
                    val stats = dashboardService.getStats()
                    call.respond(KResult.ok(stats))
                }
            }

            requirePermission("monitor:dashboard:view") {
                get(MonitorApiRoutes.Dashboard.SYSTEM_INFO) {
                    val systemInfo = dashboardService.getSystemInfo()
                    call.respond(KResult.ok(systemInfo))
                }
            }
        }
    }
}
