package com.kairowan.app.lifecycle.stages

import com.kairowan.app.api.AppApiRoutes
import com.kairowan.app.lifecycle.ApplicationLifecycleStage
import com.kairowan.app.lifecycle.PrometheusRegistryKey
import com.kairowan.common.KResult
import com.kairowan.common.constant.ResultCode
import com.kairowan.core.framework.cache.CacheProvider
import com.kairowan.system.controller.authenticatedSystemRoutes
import com.kairowan.system.controller.systemRoutes
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.http.content.staticFiles
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject
import org.ktorm.database.Database
import org.slf4j.Logger

/**
 * 启动阶段：路由装配。
 *
 * 注册静态资源、健康检查、指标端点及系统业务路由。
 */
class RoutingConfigurationStage : ApplicationLifecycleStage {
    override fun execute(application: Application, logger: Logger) {
        application.routing {
            val uploadPath = application.environment.config.propertyOrNull("file.uploadPath")?.getString() ?: "uploads"
            staticFiles("/files", java.io.File(uploadPath))

            systemRoutes()

            val database by application.inject<Database>()
            val cacheProvider by application.inject<CacheProvider>()

            get(AppApiRoutes.HEALTH) {
                call.respond(KResult.ok(mapOf("status" to "UP")))
            }

            get(AppApiRoutes.READY) {
                val checks = linkedMapOf<String, String>()
                var ok = true

                try {
                    database.useConnection { conn ->
                        conn.prepareStatement("SELECT 1").execute()
                    }
                    checks["db"] = "UP"
                } catch (e: Exception) {
                    ok = false
                    checks["db"] = "DOWN"
                }

                try {
                    val cacheProbeKey = "health:ping:${System.currentTimeMillis()}"
                    val cacheProbeValue = "pong"
                    cacheProvider.set(cacheProbeKey, cacheProbeValue, 10)
                    val cacheValue = cacheProvider.get(cacheProbeKey)
                    cacheProvider.delete(cacheProbeKey)
                    if (cacheValue == cacheProbeValue) {
                        checks["cache"] = "UP"
                    } else {
                        ok = false
                        checks["cache"] = "DOWN"
                    }
                } catch (e: Exception) {
                    ok = false
                    checks["cache"] = "DOWN"
                }

                val status = if (ok) HttpStatusCode.OK else HttpStatusCode.ServiceUnavailable
                val result = if (ok) {
                    KResult.ok(checks)
                } else {
                    KResult(ResultCode.ERROR.code, "Service Unavailable", checks)
                }
                call.respond(status, result)
            }

            val metricsPath = application.environment.config.propertyOrNull("metrics.path")?.getString() ?: "/metrics"
            val registry = application.attributes.getOrNull(PrometheusRegistryKey)
            if (registry != null) {
                get(metricsPath) {
                    call.respondText(registry.scrape(), ContentType.Text.Plain)
                }
            }

            get(AppApiRoutes.ROOT) {
                call.respond(KResult.ok("Hello Kairowan-Ktor Enterprise!", "Welcome to Modular Architecture"))
            }

            authenticatedSystemRoutes()
        }
    }
}
