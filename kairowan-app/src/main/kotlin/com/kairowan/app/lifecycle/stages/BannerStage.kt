package com.kairowan.app.lifecycle.stages

import com.kairowan.app.lifecycle.ApplicationLifecycleStage
import io.ktor.server.application.Application
import org.slf4j.Logger

/**
 * 启动阶段：输出启动 Banner 与访问地址。
 */
class BannerStage : ApplicationLifecycleStage {
    override fun execute(application: Application, logger: Logger) {
        logger.info(
            """
      _  __     _
     | |/ /    (_)
     | ' / __ _ _ _ __ _____      ____ _ _ __
     |  < / _` | | '__/ _ \ \ /\ / / _` | '_ \
     | . \ (_| | | | | (_) \ V  V / (_| | | | |
     |_|\_\__,_|_|_|  \___/ \_/\_/ \__,_|_| |_|

     :: Kairowan Ktor Enterprise ::  (v2.0.0 - Modular)
    """.trimIndent()
        )

        val port = application.environment.config.propertyOrNull("ktor.deployment.port")?.getString() ?: "8080"
        logger.info("Application initialized successfully.")
        logger.info("Server: http://localhost:$port")
    }
}
