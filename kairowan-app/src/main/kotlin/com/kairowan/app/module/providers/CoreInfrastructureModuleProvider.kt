package com.kairowan.app.module.providers

import com.kairowan.app.module.AppModuleProvider
import com.kairowan.core.cache.TwoLevelCacheProvider
import com.kairowan.core.controller.CommonController
import com.kairowan.core.controller.PublicRouteController
import com.kairowan.core.framework.cache.CacheProvider
import com.kairowan.core.framework.cache.RedisCacheProvider
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.ApplicationConfig
import org.flywaydb.core.Flyway
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import org.ktorm.database.Database
import org.slf4j.LoggerFactory
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import java.sql.Connection

/**
 * 核心基础设施模块提供者。
 *
 * 负责基础 Bean：配置、数据库、Flyway、Redis 与缓存实现。
 */
class CoreInfrastructureModuleProvider : AppModuleProvider {
    override fun provide(config: ApplicationConfig): Module = module {
        val logger = LoggerFactory.getLogger("AppModules")

        single { config }

        single { CommonController() } bind PublicRouteController::class

        single {
            val dbConfig = config.config("db")
            val hikariConfig = HikariConfig().apply {
                driverClassName = dbConfig.property("driver").getString()
                jdbcUrl = dbConfig.property("url").getString()
                username = dbConfig.property("user").getString()
                password = dbConfig.property("password").getString()

                maximumPoolSize = dbConfig.propertyOrNull("hikari.maximumPoolSize")?.getString()?.toInt() ?: 20
                minimumIdle = dbConfig.propertyOrNull("hikari.minimumIdle")?.getString()?.toInt() ?: 5
                connectionTimeout = dbConfig.propertyOrNull("hikari.connectionTimeout")?.getString()?.toLong() ?: 10000
                idleTimeout = dbConfig.propertyOrNull("hikari.idleTimeout")?.getString()?.toLong() ?: 600000
                maxLifetime = dbConfig.propertyOrNull("hikari.maxLifetime")?.getString()?.toLong() ?: 1800000

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

            val flywayEnabled = dbConfig.propertyOrNull("flyway.enabled")?.getString()?.toBoolean() ?: false
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
                    .validateOnMigrate(false)
                    .cleanDisabled(true)
                    .connectRetries(3)
                    .connectRetriesInterval(1)
                    .skipDefaultCallbacks(true)
                    .skipDefaultResolvers(false)
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
                    !flywayEnabled -> "disabled by configuration (using latest schema as-is)"
                    flywayRunOnce && !flywayForce -> "runOnce enabled and migration history exists"
                    else -> "skipped by configuration"
                }
                logger.info("Flyway migrations skipped ({})", reason)
            }

            dataSource
        }

        single {
            val dataSource = get<HikariDataSource>()
            Database.connect(dataSource)
        }

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

        single { RedisCacheProvider(get()) }
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
}
