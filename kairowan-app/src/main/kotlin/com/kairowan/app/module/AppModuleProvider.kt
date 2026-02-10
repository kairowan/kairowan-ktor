package com.kairowan.app.module

import io.ktor.server.config.ApplicationConfig
import org.koin.core.module.Module

/**
 * Koin 模块提供者。
 *
 * 每个业务域通过实现该接口暴露自己的 Koin Module，
 * 由统一注册器按顺序组装。
 */
interface AppModuleProvider {
    fun provide(config: ApplicationConfig): Module
}
