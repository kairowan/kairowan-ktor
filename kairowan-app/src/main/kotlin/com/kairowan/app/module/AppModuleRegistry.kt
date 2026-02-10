package com.kairowan.app.module

import com.kairowan.app.module.providers.AuthenticationModuleProvider
import com.kairowan.app.module.providers.CoreInfrastructureModuleProvider
import com.kairowan.app.module.providers.GeneratorBusinessModuleProvider
import com.kairowan.app.module.providers.MonitorBusinessModuleProvider
import com.kairowan.app.module.providers.SystemManagementModuleProvider
import io.ktor.server.config.ApplicationConfig
import org.koin.core.module.Module

/**
 * 模块注册器。
 *
 * 负责收集所有 `AppModuleProvider` 并构建最终 Koin 模块列表。
 */
class AppModuleRegistry(
    private val providers: List<AppModuleProvider> = defaultAppModuleProviders()
) {
    fun build(config: ApplicationConfig): List<Module> =
        providers.map { provider -> provider.provide(config) }
}

fun defaultAppModuleProviders(): List<AppModuleProvider> = listOf(
    CoreInfrastructureModuleProvider(),
    AuthenticationModuleProvider(),
    SystemManagementModuleProvider(),
    MonitorBusinessModuleProvider(),
    GeneratorBusinessModuleProvider()
)
