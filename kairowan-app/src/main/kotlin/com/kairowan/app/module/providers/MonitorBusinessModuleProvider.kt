package com.kairowan.app.module.providers

import com.kairowan.app.module.AppModuleProvider
import com.kairowan.core.controller.AuthenticatedRouteController
import com.kairowan.monitor.controller.AnalysisController
import com.kairowan.monitor.controller.CacheMonitorController
import com.kairowan.monitor.controller.DashboardController
import com.kairowan.monitor.controller.MonitorController
import com.kairowan.monitor.service.AnalysisService
import com.kairowan.monitor.service.DashboardService
import com.kairowan.monitor.service.OnlineUserService
import com.kairowan.monitor.service.ServerMonitorService
import com.kairowan.monitor.service.SysJobService
import com.kairowan.monitor.service.SysLogService
import io.ktor.server.config.ApplicationConfig
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * 监控业务模块提供者。
 *
 * 注册在线用户、任务、日志、仪表盘、分析等监控能力。
 */
class MonitorBusinessModuleProvider : AppModuleProvider {
    override fun provide(config: ApplicationConfig): Module = module {
        single { OnlineUserService(get()) }
        single { ServerMonitorService(get()) }
        single { SysJobService(get()) }
        single { SysLogService(get()) }
        single { DashboardService(get()) }
        single { AnalysisService(get()) }

        single { MonitorController() } bind AuthenticatedRouteController::class
        single { DashboardController() } bind AuthenticatedRouteController::class
        single { CacheMonitorController() } bind AuthenticatedRouteController::class
        single { AnalysisController() } bind AuthenticatedRouteController::class
    }
}
