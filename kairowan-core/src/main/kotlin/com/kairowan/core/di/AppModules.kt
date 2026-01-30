package com.kairowan.ktor.di

import com.kairowan.ktor.core.async.BackgroundExecutor
import com.kairowan.ktor.core.cache.CacheProvider
import com.kairowan.ktor.core.cache.RedisCacheProvider
import com.kairowan.ktor.core.database.DatabaseProvider
import com.kairowan.ktor.core.messaging.MessageBroker
import com.kairowan.ktor.core.messaging.WebSocketBroker
import com.kairowan.ktor.core.scheduling.QuartzScheduler
import com.kairowan.ktor.core.scheduling.TaskScheduler
import com.kairowan.ktor.modules.auth.service.*
import com.kairowan.ktor.modules.job.SysJobService
import com.kairowan.ktor.modules.monitor.online.OnlineUserService
import com.kairowan.ktor.modules.monitor.server.ServerMonitorService
import com.kairowan.ktor.modules.system.menu.SysMenuService
import com.kairowan.ktor.modules.system.permission.PermissionService
import com.kairowan.ktor.modules.system.user.SysUserService
import io.ktor.server.config.*
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * 应用依赖注入模块配置
 * 
 * @author Kairowan
 * @date 2026-01-19
 */

/**
 * 核心基础设施模块
 */
fun coreModule(config: ApplicationConfig) = module {
    // 配置
    single { config }
    
    // 缓存
    single { RedisCacheProvider(config) } bind CacheProvider::class
    
    // 数据库
    single { DatabaseProvider(config) }
    
    // 任务调度
    single { QuartzScheduler() } bind TaskScheduler::class
    
    // 消息代理
    single { WebSocketBroker() } bind MessageBroker::class
    
    // 后台执行器
    single { BackgroundExecutor() }
}

/**
 * 认证模块
 */
fun authModule() = module {
    single { TokenService(get()) }
    single { CaptchaService(get()) }
    single { IdempotencyService(get()) }
    single { LoginService(get(), get(), get(), get()) }
}

/**
 * 系统管理模块
 */
fun systemModule() = module {
    single { PermissionService(get()) }
    single { SysUserService(get()) }
    single { SysMenuService(get()) }
}

/**
 * 监控模块
 */
fun monitorModule() = module {
    single { OnlineUserService(get()) }
    single { ServerMonitorService() }
}

/**
 * 任务模块
 */
fun jobModule() = module {
    single { SysJobService(get(), get()) }
}

/**
 * 日志模块
 */
fun logModule() = module {
    single { com.kairowan.ktor.framework.web.service.SysOperLogService(get(), get()) }
    single { com.kairowan.ktor.framework.web.service.SysLoginLogService(get(), get()) }
}

/**
 * 扩展模块 (其他框架服务)
 */
fun extModule() = module {
    single { com.kairowan.ktor.framework.web.service.SysDictService(get(), get()) }
    single { com.kairowan.ktor.framework.web.service.SysLoginService(get(), get(), get(), get()) }
    single { com.kairowan.ktor.framework.web.service.SysConfigService(get(), get()) }
    single { com.kairowan.ktor.framework.web.service.OnlineUserService(get()) }
    single { com.kairowan.ktor.framework.web.service.CaptchaService(get()) }
}

/**
 * 获取所有应用模块
 */
fun appModules(config: ApplicationConfig) = listOf(
    coreModule(config),
    authModule(),
    systemModule(),
    monitorModule(),
    jobModule(),
    logModule(),
    extModule()
)
