package com.kairowan.app

import com.kairowan.core.cache.TwoLevelCacheProvider
import com.kairowan.core.framework.cache.CacheProvider
import com.kairowan.core.framework.cache.RedisCacheProvider
import com.kairowan.system.service.*
import com.kairowan.monitor.service.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import org.flywaydb.core.Flyway
import org.koin.dsl.bind
import org.koin.dsl.module
import org.ktorm.database.Database
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig

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

    // 数据库连接池 (HikariCP)
    single {
        val dbConfig = config.config("db")
        val hikariConfig = HikariConfig().apply {
            driverClassName = dbConfig.property("driver").getString()
            jdbcUrl = dbConfig.property("url").getString()
            username = dbConfig.property("user").getString()
            password = dbConfig.property("password").getString()

            // HikariCP 连接池配置
            maximumPoolSize = dbConfig.propertyOrNull("hikari.maximumPoolSize")?.getString()?.toInt() ?: 20
            minimumIdle = dbConfig.propertyOrNull("hikari.minimumIdle")?.getString()?.toInt() ?: 5
            connectionTimeout = dbConfig.propertyOrNull("hikari.connectionTimeout")?.getString()?.toLong() ?: 10000
            idleTimeout = dbConfig.propertyOrNull("hikari.idleTimeout")?.getString()?.toLong() ?: 600000
            maxLifetime = dbConfig.propertyOrNull("hikari.maxLifetime")?.getString()?.toLong() ?: 1800000

            // 使用 JDBC4 isValid() 方法代替 SELECT 1，更快
            val testQuery = dbConfig.propertyOrNull("hikari.connectionTestQuery")?.getString()
            if (!testQuery.isNullOrBlank()) {
                connectionTestQuery = testQuery
            }

            isAutoCommit = dbConfig.propertyOrNull("hikari.autoCommit")?.getString()?.toBoolean() ?: true
            poolName = dbConfig.propertyOrNull("hikari.poolName")?.getString() ?: "KairowanHikariPool"
            leakDetectionThreshold = dbConfig.propertyOrNull("hikari.leakDetectionThreshold")?.getString()?.toLong() ?: 60000
            initializationFailTimeout = dbConfig.propertyOrNull("hikari.initializationFailTimeout")?.getString()?.toLong() ?: -1
        }
        val dataSource = HikariDataSource(hikariConfig)

        // 执行 Flyway 数据库迁移
        println("🔄 Running Flyway database migrations...")
        val flywayStartTime = System.currentTimeMillis()

        val flyway = Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .validateOnMigrate(false)  // 禁用迁移时验证，提高性能
            .cleanDisabled(true)        // 禁用 clean 命令，防止误删数据
            .connectRetries(3)          // 连接重试次数
            .connectRetriesInterval(1)  // 重试间隔（秒）
            .load()

        try {
            val result = flyway.migrate()
            val flywayDuration = System.currentTimeMillis() - flywayStartTime
            println("✅ Flyway migration completed: ${result.migrationsExecuted} migrations executed in ${flywayDuration}ms")
        } catch (e: Exception) {
            if (e.message?.contains("failed validation") == true) {
                println("⚠️  Flyway validation failed, attempting repair...")
                flyway.repair()
                println("✅ Flyway repair completed, retrying migration...")
                val result = flyway.migrate()
                val flywayDuration = System.currentTimeMillis() - flywayStartTime
                println("✅ Flyway migration completed: ${result.migrationsExecuted} migrations executed in ${flywayDuration}ms")
            } else {
                throw e
            }
        }

        dataSource
    }

    // Ktorm Database
    single {
        val dataSource = get<HikariDataSource>()
        Database.connect(dataSource)
    }

    // Redis 连接池
    single {
        val redisConfig = config.config("redis")
        val host = redisConfig.property("host").getString()
        val port = redisConfig.property("port").getString().toInt()
        val password = redisConfig.propertyOrNull("password")?.getString()

        val poolConfig = JedisPoolConfig().apply {
            maxTotal = redisConfig.propertyOrNull("pool.maxTotal")?.getString()?.toInt() ?: 50
            maxIdle = redisConfig.propertyOrNull("pool.maxIdle")?.getString()?.toInt() ?: 10
            minIdle = redisConfig.propertyOrNull("pool.minIdle")?.getString()?.toInt() ?: 5
            maxWaitMillis = redisConfig.propertyOrNull("pool.maxWaitMillis")?.getString()?.toLong() ?: 3000
            testOnBorrow = redisConfig.propertyOrNull("pool.testOnBorrow")?.getString()?.toBoolean() ?: true
            testWhileIdle = redisConfig.propertyOrNull("pool.testWhileIdle")?.getString()?.toBoolean() ?: true
        }

        if (password.isNullOrBlank()) {
            JedisPool(poolConfig, host, port)
        } else {
            JedisPool(poolConfig, host, port, 2000, password)
        }
    }

    // Redis 缓存提供者 (L2)
    single { RedisCacheProvider(get()) }

    // 两级缓存提供者 (L1 + L2)
    single { TwoLevelCacheProvider(get<RedisCacheProvider>()) } bind CacheProvider::class
}

/**
 * 认证模块
 */
fun authModule() = module {
    single { TokenService(get()) }
    single { CaptchaService(get()) }
    single { SysLoginService(get(), get(), get(), get()) }
}

/**
 * 系统管理模块
 */
fun systemModule() = module {
    single { SysPermissionService(get(), get()) }
    single { SysUserService(get(), get()) }
    single { SysMenuService(get(), get()) }
    single { SysRoleService(get(), get()) }
    single { SysDictService(get(), get()) }
    single { SysConfigService(get(), get()) }
    single { SysDeptService(get()) }
    single { SysPostService(get()) }
    single { ProfileService(get()) }
    single { NotificationService(get()) }
    single { FileService(get()) }
    single { FileSyncService(get()) }
}

/**
 * 监控模块
 */
fun monitorModule() = module {
    single { OnlineUserService(get()) }
    single { ServerMonitorService(get()) }
    single { SysJobService(get()) }
    single { SysLogService(get()) }
    single { DashboardService(get()) }
    single { AnalysisService(get()) }
}

/**
 * 所有模块
 */
fun allModules(config: ApplicationConfig) = listOf(
    coreModule(config),
    authModule(),
    systemModule(),
    monitorModule()
)
