package com.kairowan.ktor.framework.koin

import com.kairowan.ktor.framework.config.DatabaseFactory
import com.kairowan.ktor.framework.file.MinioClient
import com.kairowan.ktor.framework.web.service.*
import io.ktor.server.config.*
import org.koin.dsl.module

fun appModule(config: ApplicationConfig) = module {
    // Database
    single { DatabaseFactory.init(config) }
    
    // Core Services
    single { TokenService(config) }
    single { SysUserService(get()) }
    
    // Permission & Auth Services
    single { SysPermissionService(get()) }
    single { SysLoginService(get(), get(), get()) }
    
    // Business Services
    single { SysRoleService(get()) }
    single { SysMenuService(get()) }
    
    // Config & Dict Services
    single { SysConfigService(get()) }
    single { SysDictService(get()) }
    
    // Log Services
    single { SysOperLogService(get()) }
    single { SysLoginLogService(get()) }
    
    // Organization Services
    single { SysDeptService(get()) }
    single { SysPostService(get()) }
    
    // Security & Monitor Services
    single { CaptchaService() }
    single { OnlineUserService() }
    single { ServerMonitorService() }
    
    // Job Management
    single { SysJobService(get()) }
    
    // File Storage (MinIO - 使用配置或环境变量)
    single<MinioClient?> { 
        try {
            val endpoint = config.propertyOrNull("minio.endpoint")?.getString() ?: "http://localhost:9000"
            val accessKey = config.propertyOrNull("minio.accessKey")?.getString() ?: "minioadmin"
            val secretKey = config.propertyOrNull("minio.secretKey")?.getString() ?: "minioadmin"
            MinioClient(endpoint, accessKey, secretKey).also { it.init() }
        } catch (e: Exception) {
            null // MinIO 不可用时返回 null
        }
    }
    single { SysFileService(get(), getOrNull()) }
}
