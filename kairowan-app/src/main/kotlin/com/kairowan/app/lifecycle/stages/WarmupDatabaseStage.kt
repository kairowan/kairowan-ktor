package com.kairowan.app.lifecycle.stages

import com.kairowan.app.lifecycle.ApplicationLifecycleStage
import io.ktor.server.application.Application
import org.koin.ktor.ext.inject
import org.ktorm.database.Database
import org.slf4j.Logger

/**
 * 启动阶段：数据库预热。
 *
 * 按配置触发连接初始化并执行一次连接有效性检查。
 */
class WarmupDatabaseStage : ApplicationLifecycleStage {
    override fun execute(application: Application, logger: Logger) {
        val warmupEnabled = application.environment.config.propertyOrNull("db.warmup.enabled")
            ?.getString()?.toBoolean() ?: true
        if (warmupEnabled) {
            warmupDatabase(application, logger)
        } else {
            logger.info("Database warmup skipped (disabled by configuration)")
        }
    }

    private fun warmupDatabase(application: Application, logger: Logger) {
        try {
            logger.info("Warming up database connection...")
            val startTime = System.currentTimeMillis()

            val t1 = System.currentTimeMillis()
            val database by application.inject<Database>()
            val t2 = System.currentTimeMillis()
            logger.info("Ktorm Database.connect() took ${t2 - t1}ms")

            val t3 = System.currentTimeMillis()
            database.useConnection { conn ->
                if (!conn.isValid(3)) {
                    throw IllegalStateException("Database connection is not valid")
                }
            }
            val t4 = System.currentTimeMillis()
            logger.info("Connection validation took ${t4 - t3}ms")

            val duration = System.currentTimeMillis() - startTime
            logger.info("Database warmup completed in ${duration}ms")
        } catch (e: Exception) {
            logger.error("Database warmup failed", e)
            throw e
        }
    }
}
