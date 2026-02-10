package com.kairowan.app.lifecycle.stages

import com.kairowan.app.lifecycle.ApplicationLifecycleStage
import com.kairowan.app.lifecycle.PrometheusRegistryKey
import com.kairowan.core.framework.web.plugin.RequestLogPlugin
import com.kairowan.core.req.LoginReq
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.pluginOrNull
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.doublereceive.DoubleReceive
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.websocket.*
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import org.slf4j.Logger
import java.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * 启动阶段：安装与配置通用 Ktor 插件。
 *
 * 包含 CORS、序列化、限流、校验、WebSocket 与指标采集。
 */
class PluginConfigurationStage : ApplicationLifecycleStage {
    override fun execute(application: Application, logger: Logger) {
        application.install(CORS) {
            allowMethod(HttpMethod.Options)
            allowMethod(HttpMethod.Get)
            allowMethod(HttpMethod.Post)
            allowMethod(HttpMethod.Put)
            allowMethod(HttpMethod.Delete)
            allowMethod(HttpMethod.Patch)
            allowHeader(HttpHeaders.Authorization)
            allowHeader(HttpHeaders.ContentType)
            allowHeader(HttpHeaders.AccessControlAllowOrigin)
            allowCredentials = true

            val allowedOrigins = application.environment.config.propertyOrNull("cors.allowedOrigins")
                ?.getString()
                ?.split(",")
                ?: listOf("http://localhost:3000", "http://localhost:5173", "http://localhost:8081")

            allowedOrigins.forEach { origin ->
                allowHost(origin.removePrefix("http://").removePrefix("https://"), schemes = listOf("http", "https"))
            }
        }

        application.install(DoubleReceive)
        application.install(RequestLogPlugin)

        application.install(ContentNegotiation) {
            jackson {
                findAndRegisterModules()
                disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                disable(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            }
        }

        application.install(WebSockets) {
            pingPeriod = Duration.ofSeconds(15)
            timeout = Duration.ofSeconds(30)
            maxFrameSize = Long.MAX_VALUE
            masking = false
        }

        application.install(RateLimit) {
            global {
                rateLimiter(limit = 100, refillPeriod = 60.seconds)
            }
        }

        application.install(RequestValidation) {
            validate<LoginReq> { body ->
                val errors = mutableListOf<String>()
                if (body.username.isBlank()) errors.add("username is blank")
                if (body.password.isBlank()) errors.add("password is blank")
                if (body.code.isNullOrBlank()) errors.add("captcha code is blank")
                if (body.uuid.isNullOrBlank()) errors.add("captcha uuid is blank")
                if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
            }
        }

        val metricsEnabled = application.environment.config.propertyOrNull("metrics.enabled")
            ?.getString()
            ?.toBooleanStrictOrNull()
            ?: true
        if (metricsEnabled) {
            val registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
            ClassLoaderMetrics().bindTo(registry)
            JvmMemoryMetrics().bindTo(registry)
            JvmGcMetrics().bindTo(registry)
            JvmThreadMetrics().bindTo(registry)
            ProcessorMetrics().bindTo(registry)
            if (application.pluginOrNull(MicrometerMetrics) == null) {
                application.install(MicrometerMetrics) {
                    this.registry = registry
                }
            }
            application.attributes.put(PrometheusRegistryKey, registry)
        }
    }
}
