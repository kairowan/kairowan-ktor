package com.kairowan.monitor.controller

import com.kairowan.common.KResult
import com.kairowan.core.controller.AuthenticatedRouteController
import com.kairowan.core.controller.KController
import com.kairowan.core.framework.security.requirePermission
import com.kairowan.monitor.api.MonitorApiRoutes
import com.kairowan.monitor.service.AnalysisService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

/**
 * 数据分析控制器
 * @author Kairowan
 * @date 2026-01-29
 */
class AnalysisController : KController(), AuthenticatedRouteController {

    override fun register(route: Route) {
        route.routes()
    }

    private fun Route.routes() {
        val analysisService by inject<AnalysisService>()

        route(MonitorApiRoutes.Analysis.ROOT) {
            // 获取数据概览
            requirePermission("monitor:analysis:view") {
                get(MonitorApiRoutes.Analysis.OVERVIEW) {
                    val startDate = call.request.queryParameters["startDate"]
                    val endDate = call.request.queryParameters["endDate"]

                    val overview = analysisService.getOverview(startDate, endDate)
                    call.respond(KResult.ok(overview))
                }
            }

            // 获取销售趋势数据
            requirePermission("monitor:analysis:view") {
                get(MonitorApiRoutes.Analysis.SALES_TREND) {
                    val startDate = call.request.queryParameters["startDate"]
                    val endDate = call.request.queryParameters["endDate"]
                    val type = call.request.queryParameters["type"] ?: "month"

                    val trend = analysisService.getSalesTrend(startDate, endDate, type)
                    call.respond(KResult.ok(trend))
                }
            }

            // 获取分类占比数据
            requirePermission("monitor:analysis:view") {
                get(MonitorApiRoutes.Analysis.CATEGORY) {
                    val category = analysisService.getCategoryData()
                    call.respond(KResult.ok(category))
                }
            }

            // 获取地区销售数据
            requirePermission("monitor:analysis:view") {
                get(MonitorApiRoutes.Analysis.REGION) {
                    val region = analysisService.getRegionData()
                    call.respond(KResult.ok(region))
                }
            }

            // 获取用户增长数据
            requirePermission("monitor:analysis:view") {
                get(MonitorApiRoutes.Analysis.USER_GROWTH) {
                    val growth = analysisService.getUserGrowth()
                    call.respond(KResult.ok(growth))
                }
            }

            // 导出报表
            requirePermission("monitor:analysis:export") {
                get(MonitorApiRoutes.Analysis.EXPORT) {
                    val startDate = call.request.queryParameters["startDate"]
                    val endDate = call.request.queryParameters["endDate"]
                    val type = call.request.queryParameters["type"] ?: "sales"

                    val data = analysisService.exportReport(startDate, endDate, type)

                    // TODO: 实现真实的Excel导出
                    // 这里暂时返回提示信息
                    call.respond(KResult.ok<Any>(msg = "报表导出功能开发中"))
                }
            }
        }
    }
}
