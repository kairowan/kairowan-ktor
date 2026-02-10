package com.kairowan.app.module.providers

import com.kairowan.app.module.AppModuleProvider
import com.kairowan.core.controller.AuthenticatedRouteController
import com.kairowan.generator.controller.GenController
import io.ktor.server.config.ApplicationConfig
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * 代码生成模块提供者。
 *
 * 注册代码生成控制器与相关依赖。
 */
class GeneratorBusinessModuleProvider : AppModuleProvider {
    override fun provide(config: ApplicationConfig): Module = module {
        single { GenController() } bind AuthenticatedRouteController::class
    }
}
