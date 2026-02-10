package com.kairowan.app.lifecycle

import com.kairowan.app.lifecycle.stages.AuthenticationConfigurationStage
import com.kairowan.app.lifecycle.stages.BannerStage
import com.kairowan.app.lifecycle.stages.ExceptionHandlingStage
import com.kairowan.app.lifecycle.stages.FileSyncOnStartupStage
import com.kairowan.app.lifecycle.stages.PluginConfigurationStage
import com.kairowan.app.lifecycle.stages.RoutingConfigurationStage
import com.kairowan.app.lifecycle.stages.ValidateConfigurationStage
import com.kairowan.app.lifecycle.stages.WarmupDatabaseStage
import io.ktor.server.application.Application
import org.slf4j.Logger

/**
 * 生命周期阶段注册器。
 *
 * 按定义顺序执行全部启动阶段，作为应用启动编排入口。
 */
class ApplicationLifecycleRegistry(
    private val stages: List<ApplicationLifecycleStage> = defaultLifecycleStages()
) {
    fun run(application: Application, logger: Logger) {
        stages.forEach { stage -> stage.execute(application, logger) }
    }
}

fun defaultLifecycleStages(): List<ApplicationLifecycleStage> = listOf(
    ValidateConfigurationStage(),
    WarmupDatabaseStage(),
    FileSyncOnStartupStage(),
    PluginConfigurationStage(),
    AuthenticationConfigurationStage(),
    ExceptionHandlingStage(),
    RoutingConfigurationStage(),
    BannerStage()
)
