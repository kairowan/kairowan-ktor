package com.kairowan.ktor

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.kairowan.ktor.common.KResult
import com.kairowan.ktor.common.constant.ResultCode
import com.kairowan.ktor.common.exception.ServiceException
import com.kairowan.ktor.core.cache.CacheProvider
import com.kairowan.ktor.core.database.DatabaseProvider
import com.kairowan.ktor.core.async.BackgroundExecutor
import com.kairowan.ktor.core.scheduling.TaskScheduler
import com.kairowan.ktor.di.appModules
import com.kairowan.ktor.framework.web.dto.LoginBody as FrameworkLoginBody
import com.kairowan.ktor.modules.auth.controller.LoginBody
import com.kairowan.ktor.modules.auth.controller.authRoutes
import com.kairowan.ktor.modules.auth.controller.authenticatedAuthRoutes
import com.kairowan.ktor.modules.auth.controller.idempotentRoutes
import com.kairowan.ktor.modules.auth.model.LoginUser
import com.kairowan.ktor.modules.job.controller.sysJobRoutes
import com.kairowan.ktor.modules.monitor.controller.monitorRoutes
import com.kairowan.ktor.modules.websocket.webSocketApiRoutes
import com.kairowan.ktor.modules.websocket.webSocketRoutes
import com.kairowan.ktor.framework.web.plugin.RequestLogPlugin
import io.github.smiley4.ktorswaggerui.SwaggerUI
import io.github.smiley4.ktorswaggerui.data.AuthScheme
import io.github.smiley4.ktorswaggerui.data.AuthType
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.doublereceive.*
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.util.AttributeKey
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Kairowan Ktor Application
 * 模块化架构 + 依赖注入
 *
 * @author Kairowan
 * @date 2026-01-19
 */
fun main(args: Array<String>): Unit = EngineMain.main(args)

private const val TOKEN_BLACKLIST_PREFIX = "token:blacklist:"
private val PrometheusRegistryKey = AttributeKey<PrometheusMeterRegistry>("PrometheusRegistry")

fun Application.module() {
    val logger = LoggerFactory.getLogger("Application")

    // ===== 1. 依赖注入 =====
    if (pluginOrNull(Koin) == null) {
        install(Koin) {
            modules(appModules(environment.config))
        }
    }

    validateConfiguration(logger)

    // ===== 2. 插件配置 =====
    configurePlugins()

    // ===== 3. 认证配置 =====
    configureAuthentication()

    // ===== 4. 异常处理 =====
    configureExceptionHandling(logger)

    // ===== 5. 路由配置 =====
    configureRouting()

    // ===== 6. 生命周期 =====
    configureLifecycle(logger)

    // ===== 7. 启动信息 =====
    printBanner(logger)
}

/**
 * 配置插件
 */
private fun Application.validateConfiguration(logger: org.slf4j.Logger) {
    val isDev = environment.developmentMode
    val jwtSecret = environment.config.property("jwt.secret").getString()
    val dbPassword = environment.config.propertyOrNull("db.password")?.getString()

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

private fun Application.configurePlugins() {
    // 允许请求体被多次读取
    install(DoubleReceive)

    // JSON 序列化
    install(ContentNegotiation) {
        jackson { }
    }

    // WebSocket 支持
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(30)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    // 限流
    install(RateLimit) {
        global {
            rateLimiter(limit = 100, refillPeriod = 60.seconds)
        }
    }

    // Swagger UI
    install(SwaggerUI) {
        swagger {
            swaggerUrl = "swagger-ui"
            forwardRoot = true
        }
        info {
            title = "Kairowan Ktor Enterprise API"
            version = "latest"
            description = "API Documentation - Modular Architecture"
        }
        server {
            url = "http://localhost:8080"
            description = "Local Server"
        }
        securityScheme("BearerAuth") {
            type = AuthType.HTTP
            scheme = AuthScheme.BEARER
            bearerFormat = "JWT"
        }
    }

    // 请求验证
    install(RequestValidation) {
        validate<LoginBody> { body ->
            val errors = mutableListOf<String>()
            if (body.username.isBlank()) errors.add("username is blank")
            if (body.password.isBlank()) errors.add("password is blank")
            if (body.code.isNullOrBlank()) errors.add("captcha code is blank")
            if (body.uuid.isNullOrBlank()) errors.add("captcha uuid is blank")
            if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
        }

        validate<FrameworkLoginBody> { body ->
            val errors = mutableListOf<String>()
            if (body.username.isBlank()) errors.add("username is blank")
            if (body.password.isBlank()) errors.add("password is blank")
            if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
        }
        // 按需配置
    }


    val metricsEnabled = environment.config.propertyOrNull("metrics.enabled")
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
        if (pluginOrNull(MicrometerMetrics) == null) {
            install(MicrometerMetrics) {
                this.registry = registry
            }
        }
        attributes.put(PrometheusRegistryKey, registry)
    }
    val requestLogEnabled = environment.config.propertyOrNull("logging.request.enabled")
        ?.getString()
        ?.toBooleanStrictOrNull()
        ?: environment.developmentMode
    if (requestLogEnabled) {
        if (pluginOrNull(RequestLogPlugin) == null) {
            install(RequestLogPlugin)
        }
    }
}

/**
 * 配置认证
 */
private fun Application.configureAuthentication() {
    val jwtSecret = environment.config.property("jwt.secret").getString()
    val jwtIssuer = environment.config.property("jwt.issuer").getString()
    val jwtAudience = environment.config.property("jwt.audience").getString()
    val jwtRealm = environment.config.property("jwt.realm").getString()
    val cache by inject<CacheProvider>()

    install(Authentication) {
        jwt {
            realm = jwtRealm
            verifier(
                JWT.require(Algorithm.HMAC256(jwtSecret))
                    .withIssuer(jwtIssuer)
                    .withAudience(jwtAudience)
                    .build()
            )
            validate { credential ->
                val tokenId = credential.payload.id
                if (tokenId.isNullOrBlank()) return@validate null
                if (cache.exists("$TOKEN_BLACKLIST_PREFIX$tokenId")) return@validate null

                val userId = credential.payload.getClaim("userId").asInt() ?: return@validate null
                val username = credential.payload.getClaim("username").asString() ?: return@validate null
                val roles = credential.payload.getClaim("roles").asList(String::class.java)?.toSet() ?: emptySet()
                val permissions = credential.payload.getClaim("permissions").asList(String::class.java)?.toSet() ?: emptySet()

                LoginUser(userId, username, roles = roles, permissions = permissions)
            }
        }
    }
}

/**
 * 配置异常处理
 */
private fun Application.configureExceptionHandling(logger: org.slf4j.Logger) {
    install(StatusPages) {
        // 验证异常
        exception<RequestValidationException> { call, cause ->
            call.respond(KResult.fail<Any>(ResultCode.BAD_REQUEST.code, cause.reasons.joinToString()))
        }

        // 业务异常
        exception<ServiceException> { call, cause ->
            call.respond(KResult.fail<Any>(cause.code, cause.message))
        }

        // 授权异常
        status(HttpStatusCode.Unauthorized) { call, _ ->
            call.respond(HttpStatusCode.OK, KResult.fail<Any>(ResultCode.UNAUTHORIZED))
        }

        status(HttpStatusCode.Forbidden) { call, _ ->
            call.respond(HttpStatusCode.OK, KResult.fail<Any>(ResultCode.FORBIDDEN))
        }

        // 系统异常
        exception<Throwable> { call, cause ->
            logger.error("Global Exception", cause)
            val msg = if (call.application.environment.developmentMode) {
                "System Error: ${cause.localizedMessage}"
            } else {
                "System Error"
            }
            call.respond(HttpStatusCode.InternalServerError, KResult.fail<Any>(msg))
        }
    }
}

/**
 * 配置路由
 */
private fun Application.configureRouting() {
    routing {
        // ===== 公开路由 =====
        authRoutes()

        val databaseProvider by inject<DatabaseProvider>()
        val cacheProvider by inject<CacheProvider>()

        get("/health") {
            call.respond(KResult.ok(mapOf("status" to "UP")))
        }

        get("/ready") {
            val checks = linkedMapOf<String, String>()
            var ok = true

            try {
                databaseProvider.database.useConnection { conn ->
                    conn.prepareStatement("SELECT 1").execute()
                }
                checks["db"] = "UP"
            } catch (e: Exception) {
                ok = false
                checks["db"] = "DOWN"
            }

            try {
                cacheProvider.exists("health:ping")
                checks["cache"] = "UP"
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

        get("/") {
            call.respond(KResult.ok("Hello Kairowan-Ktor Enterprise!", "Welcome to Modular Architecture"))
        }

        // ===== 需要认证的路由 =====
        authenticate {
            authenticatedAuthRoutes()
            idempotentRoutes()
            monitorRoutes()
            sysJobRoutes()
            webSocketApiRoutes()
        }

        // ===== WebSocket 路由 =====
        webSocketRoutes()
    }
}

/**
 * 配置生命周期
 */
private fun Application.configureLifecycle(logger: org.slf4j.Logger) {
    val taskScheduler by inject<TaskScheduler>()
    val databaseProvider by inject<DatabaseProvider>()
    val cacheProvider by inject<CacheProvider>()
    val backgroundExecutor by inject<BackgroundExecutor>()

    environment.monitor.subscribe(ApplicationStarted) {
        taskScheduler.start()
        logger.info("TaskScheduler started.")
    }

    environment.monitor.subscribe(ApplicationStopped) {
        taskScheduler.shutdown()
        logger.info("TaskScheduler shutdown.")
        backgroundExecutor.close()
        databaseProvider.close()
        (cacheProvider as? Closeable)?.close()
    }
}

/**
 * 打印启动信息
 */
private fun Application.printBanner(logger: org.slf4j.Logger) {
    println("""
      _  __     _                                    
     | |/ /    (_)                                   
     | ' / __ _ _ _ __ _____      ____ _ _ __  
     |  < / _` | | '__/ _ \ \ /\ / / _` | '_ \ 
     | . \ (_| | | | | (_) \ V  V / (_| | | | |
     |_|\_\__,_|_|_|  \___/ \_/\_/ \__,_|_| |_|
     
     :: Kairowan Ktor Enterprise ::  (v2.0.0 - Modular)
    """.trimIndent())

    val port = environment.config.propertyOrNull("ktor.deployment.port")?.getString() ?: "8080"
    logger.info("Application initialized successfully.")
    logger.info("Swagger UI: http://localhost:$port/swagger-ui/index.html")
}
