package com.kairowan.app.lifecycle.stages

import com.kairowan.app.lifecycle.ApplicationLifecycleStage
import io.ktor.server.application.Application
import org.slf4j.Logger

/**
 * 启动阶段：校验关键配置。
 *
 * 包括 JWT 强度与生产环境敏感配置检查。
 */
class ValidateConfigurationStage : ApplicationLifecycleStage {
    override fun execute(application: Application, logger: Logger) {
        val isDev = application.environment.developmentMode
        val jwtSecret = application.environment.config.property("jwt.secret").getString()
        val dbPassword = application.environment.config.propertyOrNull("db.password")?.getString()

        if (jwtSecret.length < 32) {
            val msg = "jwt.secret length must be at least 32 characters"
            if (isDev) {
                logger.warn(msg)
            } else {
                throw IllegalStateException(msg)
            }
        }

        if (!isDev && jwtSecret.contains("kairowan-secret", ignoreCase = true)) {
            throw IllegalStateException("jwt.secret must be overridden in non-dev environments")
        }

        if (!isDev && dbPassword != null && dbPassword == "password") {
            throw IllegalStateException("db.password must be overridden in non-dev environments")
        }
    }
}
