package com.kairowan.app

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.kairowan.common.KResult
import com.kairowan.common.constant.ResultCode
import com.kairowan.common.exception.ServiceException
import com.kairowan.core.framework.security.LoginUser
import com.kairowan.core.dto.LoginBody
import com.kairowan.system.controller.systemRoutes
import com.kairowan.system.controller.authenticatedSystemRoutes
import com.kairowan.generator.controller.generatorRoutes
import com.kairowan.monitor.controller.monitorRoutes
import com.kairowan.monitor.controller.dashboardRoutes
import com.kairowan.core.framework.cache.CacheProvider
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
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
import org.ktorm.database.Database
import org.slf4j.LoggerFactory
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

private const val TOKEN_BLACKLIST_PREFIX = "login_token:blacklist:"
private val PrometheusRegistryKey = AttributeKey<PrometheusMeterRegistry>("PrometheusRegistry")

fun Application.module() {
    val logger = LoggerFactory.getLogger("Application")

    // ===== 1. 依赖注入 =====
    if (pluginOrNull(Koin) == null) {
        install(Koin) {
            modules(allModules(environment.config))
        }
    }

    validateConfiguration(logger)

    // ===== 2. 预热数据库连接 =====
    warmupDatabase(logger)

    // ===== 3. 插件配置 =====
    configurePlugins()

    // ===== 4. 认证配置 =====
    configureAuthentication()

    // ===== 5. 异常处理 =====
    configureExceptionHandling(logger)

    // ===== 6. 路由配置 =====
    configureRouting()

    // ===== 7. 启动信息 =====
    printBanner(logger)
}

/**
 * 验证配置
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

/**
 * 预热数据库连接
 * 在应用启动时初始化数据库连接池和执行迁移，避免第一个请求超时
 */
private fun Application.warmupDatabase(logger: org.slf4j.Logger) {
    try {
        logger.info("🔥 Warming up database connection...")
        val startTime = System.currentTimeMillis()

        // 从 Koin 获取 Database 实例，这会触发 HikariDataSource 和 Flyway 的初始化
        val database by inject<Database>()

        // 执行一个简单的查询来确保连接池已就绪
        database.useConnection { conn ->
            conn.prepareStatement("SELECT 1").execute()
        }

        val duration = System.currentTimeMillis() - startTime
        logger.info("✅ Database warmup completed in ${duration}ms")
    } catch (e: Exception) {
        logger.error("❌ Database warmup failed", e)
        throw e
    }
}

/**
 * 配置插件
 */
private fun Application.configurePlugins() {
    // CORS 配置
    install(CORS) {
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

        // 允许的来源
        val allowedOrigins = this@configurePlugins.environment.config.propertyOrNull("cors.allowedOrigins")
            ?.getString()
            ?.split(",")
            ?: listOf("http://localhost:3000", "http://localhost:5173", "http://localhost:8081")

        allowedOrigins.forEach { origin ->
            allowHost(origin.removePrefix("http://").removePrefix("https://"), schemes = listOf("http", "https"))
        }
    }

    // 允许请求体被多次读取
    install(DoubleReceive)

    // JSON 序列化
    install(ContentNegotiation) {
        jackson {
            // 支持 Java 8 日期时间类型
            findAndRegisterModules()
            // 禁用将日期写为时间戳
            disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            // 忽略未知属性
            disable(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        }
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
    }

    // Metrics (Prometheus)
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
        // ===== 静态文件服务 =====
        val uploadPath = this@configureRouting.environment.config.propertyOrNull("file.uploadPath")?.getString() ?: "uploads"
        staticFiles("/files", java.io.File(uploadPath))

        // ===== 公开路由 =====
        systemRoutes()

        val database by inject<Database>()
        val cacheProvider by inject<CacheProvider>()

        get("/health") {
            call.respond(KResult.ok(mapOf("status" to "UP")))
        }

        get("/ready") {
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
        authenticatedSystemRoutes()

        authenticate {
            generatorRoutes()
            monitorRoutes()
            dashboardRoutes()
        }
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
    logger.info("Server: http://localhost:$port")
}
