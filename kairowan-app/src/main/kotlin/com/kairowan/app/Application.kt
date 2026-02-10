package com.kairowan.app

import com.kairowan.app.lifecycle.ApplicationLifecycleRegistry
import com.kairowan.app.module.AppModuleRegistry
import io.ktor.server.application.*
import io.ktor.server.netty.*
import org.koin.ktor.plugin.Koin
import org.slf4j.LoggerFactory

/**
 * Kairowan Ktor Application
 * 模块化架构 + 依赖注入
 * @author Kairowan
 * @date 2026-01-19
 */
fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    val logger = LoggerFactory.getLogger("Application")

    if (pluginOrNull(Koin) == null) {
        val appModules = AppModuleRegistry().build(environment.config)
        install(Koin) {
            modules(appModules)
        }
    }

    ApplicationLifecycleRegistry().run(this, logger)
}
