package com.kairowan.app

import com.kairowan.core.cache.TwoLevelCacheProvider
import com.kairowan.core.framework.cache.CacheProvider
import com.kairowan.core.framework.cache.RedisCacheProvider
import com.kairowan.system.service.*
import com.kairowan.system.controller.*
import com.kairowan.monitor.controller.*
import com.kairowan.generator.controller.*
import com.kairowan.core.controller.PublicRouteController
import com.kairowan.core.controller.AuthenticatedRouteController
import com.kairowan.core.controller.CommonController
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
import java.sql.Connection
import org.slf4j.LoggerFactory

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
    val logger = LoggerFactory.getLogger("AppModules")
    // 配置
    single { config }

    // 公共路由控制器
    single { CommonController() } bind PublicRouteController::class

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

            // 性能优化：允许连接池在后台异步初始化
            // 这样可以加快应用启动速度，连接会在需要时才创建
            // 注意：这意味着第一个请求可能会稍慢（需要等待连接建立）
            // 如果需要预热连接，可以在 warmupDatabase() 中手动触发
        }
        val dataSource = HikariDataSource(hikariConfig)

        // 执行 Flyway 数据库迁移（可通过环境变量禁用）
        val flywayEnabled = dbConfig.propertyOrNull("flyway.enabled")?.getString()?.toBoolean() ?: true
        val flywayRunOnce = dbConfig.propertyOrNull("flyway.runOnce")?.getString()?.toBoolean() ?: true
        val flywayForce = dbConfig.propertyOrNull("flyway.force")?.getString()?.toBoolean() ?: false

        val shouldRunMigrations = when {
            !flywayEnabled -> false
            flywayForce -> true
            flywayRunOnce -> !hasFlywayHistory(dataSource)
            else -> true
        }

        if (shouldRunMigrations) {
            logger.info("Running Flyway database migrations...")
            val flywayStartTime = System.currentTimeMillis()

            val flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .validateOnMigrate(false)  // 禁用迁移时验证，提高性能
                .cleanDisabled(true)        // 禁用 clean 命令，防止误删数据
                .connectRetries(3)          // 连接重试次数
                .connectRetriesInterval(1)  // 重试间隔（秒）
                .skipDefaultCallbacks(true) // 跳过默认回调，提高性能
                .skipDefaultResolvers(false) // 保留默认解析器
                .load()

            try {
                val result = flyway.migrate()
                val flywayDuration = System.currentTimeMillis() - flywayStartTime
                logger.info("Flyway migration completed: {} migrations executed in {}ms", result.migrationsExecuted, flywayDuration)
            } catch (e: Exception) {
                if (e.message?.contains("failed validation") == true) {
                    logger.warn("Flyway validation failed, attempting repair...")
                    flyway.repair()
                    logger.info("Flyway repair completed, retrying migration...")
                    val result = flyway.migrate()
                    val flywayDuration = System.currentTimeMillis() - flywayStartTime
                    logger.info("Flyway migration completed: {} migrations executed in {}ms", result.migrationsExecuted, flywayDuration)
                } else {
                    throw e
                }
            }
        } else {
            val reason = when {
                !flywayEnabled -> "disabled by configuration"
                flywayRunOnce && !flywayForce -> "runOnce enabled and migration history exists"
                else -> "skipped by configuration"
            }
            logger.info("Flyway migrations skipped ({})", reason)
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

private fun hasFlywayHistory(dataSource: HikariDataSource): Boolean {
    dataSource.connection.use { conn ->
        if (!tableExists(conn, "flyway_schema_history")) {
            return false
        }
        conn.prepareStatement("SELECT COUNT(1) FROM flyway_schema_history").use { stmt ->
            stmt.executeQuery().use { rs ->
                return rs.next() && rs.getInt(1) > 0
            }
        }
    }
}

private fun tableExists(conn: Connection, tableName: String): Boolean {
    conn.metaData.getTables(null, null, tableName, arrayOf("TABLE")).use { rs ->
        return rs.next()
    }
}

/**
 * 认证模块
 */
fun authModule() = module {
    single { TokenService(get()) }
    single { CaptchaService(get()) }
    single { SysLoginService(get(), get(), get(), get()) }

    single { AuthController() } bind PublicRouteController::class
    single { CaptchaController() } bind PublicRouteController::class
    single { AuthenticatedController() } bind AuthenticatedRouteController::class
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

    single { ProfileController() } bind AuthenticatedRouteController::class
    single { NotificationController() } bind AuthenticatedRouteController::class
    single { FileController() } bind AuthenticatedRouteController::class
    single { ToolFileController() } bind AuthenticatedRouteController::class
    single { SysUserController() } bind AuthenticatedRouteController::class
    single { SysRoleController() } bind AuthenticatedRouteController::class
    single { SysMenuController() } bind AuthenticatedRouteController::class
    single { SysDeptController() } bind AuthenticatedRouteController::class
    single { SysPostController() } bind AuthenticatedRouteController::class
    single { SysConfigController() } bind AuthenticatedRouteController::class
    single { SysDictController() } bind AuthenticatedRouteController::class
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

    single { MonitorController() } bind AuthenticatedRouteController::class
    single { DashboardController() } bind AuthenticatedRouteController::class
    single { CacheMonitorController() } bind AuthenticatedRouteController::class
    single { AnalysisController() } bind AuthenticatedRouteController::class
}

/**
 * 代码生成模块
 */
fun generatorModule() = module {
    single { GenController() } bind AuthenticatedRouteController::class
}

/**
 * 所有模块
 */
fun allModules(config: ApplicationConfig) = listOf(
    coreModule(config),
    authModule(),
    systemModule(),
    monitorModule(),
    generatorModule()
)
