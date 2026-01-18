package com.kairowan.ktor

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.kairowan.ktor.common.KResult
import com.kairowan.ktor.common.constant.ResultCode
import com.kairowan.ktor.common.exception.ServiceException
import com.kairowan.ktor.framework.config.RedisConfig
import com.kairowan.ktor.framework.koin.appModule
import com.kairowan.ktor.framework.security.LoginUser
import com.kairowan.ktor.framework.task.CronManager
import com.kairowan.ktor.framework.web.controller.commonRoutes
import com.kairowan.ktor.framework.web.controller.sysUserRoutes
import com.kairowan.ktor.framework.web.controller.authRoutes
import com.kairowan.ktor.framework.web.controller.authenticatedAuthRoutes
import com.kairowan.ktor.framework.web.controller.sysRoleRoutes
import com.kairowan.ktor.framework.web.controller.sysMenuRoutes
import com.kairowan.ktor.framework.web.controller.sysConfigRoutes
import com.kairowan.ktor.framework.web.controller.sysDictRoutes
import com.kairowan.ktor.framework.web.controller.sysLogRoutes
import com.kairowan.ktor.framework.web.controller.sysDeptRoutes
import com.kairowan.ktor.framework.web.controller.sysPostRoutes
import com.kairowan.ktor.framework.web.controller.genRoutes
import com.kairowan.ktor.framework.web.controller.captchaRoutes
import com.kairowan.ktor.framework.web.controller.monitorRoutes
import com.kairowan.ktor.framework.web.controller.sysJobRoutes
import com.kairowan.ktor.framework.web.controller.webSocketApiRoutes
import com.kairowan.ktor.framework.web.controller.sysFileRoutes
import com.kairowan.ktor.framework.web.controller.idempotentRoutes
import com.kairowan.ktor.framework.websocket.webSocketRoutes
import io.ktor.server.websocket.*
import java.time.Duration
import io.github.smiley4.ktorswaggerui.SwaggerUI
import io.github.smiley4.ktorswaggerui.data.AuthScheme
import io.github.smiley4.ktorswaggerui.data.AuthType
import io.ktor.http.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.doublereceive.*
import com.kairowan.ktor.framework.web.plugin.RequestLogPlugin
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.request.path
import io.micrometer.prometheus.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.url
import org.koin.ktor.plugin.Koin
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import kotlin.time.Duration.Companion.seconds

/**
 * Kairowan Ktor Application
 * Main Entry Point
 *
 * @author Kairowan
 * @date 2026-01-17
 */
fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    val logger = LoggerFactory.getLogger("Application")

    // 0. Initialize Infrastructure
    RedisConfig.init(environment.config)

    // 1. Dependency Injection (Koin)
    install(Koin) {
        modules(appModule(environment.config))
    }

    // 1. Request Logging (必须在 ContentNegotiation 之前安装，才能捕获原始响应对象)
    install(DoubleReceive) // 允许请求体被多次读取
    install(RequestLogPlugin) // Custom Body Logging

    // 2. Content Negotiation (JSON) - 在 RequestLogPlugin 之后安装
    install(ContentNegotiation) {
        jackson { }
    }
    
    // 2.1 WebSocket Support
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(30)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    // 4. Rate Limiting (Concurrency Control)
    install(RateLimit) {
        global {
            rateLimiter(limit = 100, refillPeriod = 60.seconds)
        }
    }
    
    // 5. Authentication (JWT)
    val jwtSecret = environment.config.property("jwt.secret").getString()
    val jwtIssuer = environment.config.property("jwt.issuer").getString()
    val jwtAudience = environment.config.property("jwt.audience").getString()
    val jwtRealm = environment.config.property("jwt.realm").getString()
    
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
                if (credential.payload.getClaim("userId").asInt() != null) {
                    val userId = credential.payload.getClaim("userId").asInt()
                    val username = credential.payload.getClaim("username").asString()
                    LoginUser(userId, username)
                } else {
                    null
                }
            }
        }
    }



    // 6. Global Exception Handler
    install(StatusPages) {
        // Validation Exception
        exception<RequestValidationException> { call, cause ->
            call.respond(KResult.fail<Any>(ResultCode.BAD_REQUEST.code, cause.reasons.joinToString()))
        }

        // Business Exception
        exception<ServiceException> { call, cause ->
             call.respond(KResult.fail<Any>(cause.code, cause.message))
        }
        
        // Authorization Exception
        status(HttpStatusCode.Unauthorized) { call, _ ->
            call.respond(HttpStatusCode.OK, KResult.fail<Any>(ResultCode.UNAUTHORIZED))
        }
        
        status(HttpStatusCode.Forbidden) { call, _ ->
            call.respond(HttpStatusCode.OK, KResult.fail<Any>(ResultCode.FORBIDDEN))
        }
        
        // System Exception
        exception<Throwable> { call, cause ->
            logger.error("Global Exception", cause)
            call.respond(HttpStatusCode.InternalServerError, KResult.fail<Any>("System Error: ${cause.localizedMessage}"))
        }
    }

    // Add: Swagger UI
    install(SwaggerUI) {
        swagger {
            swaggerUrl = "swagger-ui"
            forwardRoot = true
        }
        info {
            title = "Kairowan Ktor Enterprise API"
            version = "latest"
            description = "API Documentation for Apifox Synchronization"
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

    // Add: Request Validation
    install(RequestValidation) {
        // Will configure per-route specific validation later
    }

    // Add: Micrometer Metrics
    val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    install(MicrometerMetrics) {
        registry = appMicrometerRegistry
        // Custom tags (optional)
        // tags = mapOf("app" to "kairowan_server", "env" to "dev")
    }

    // 7. Routing
    routing {
        // Public Routes (No Auth Required)
        commonRoutes() // File I/O
        authRoutes()   // Login
        captchaRoutes() // Captcha Image
        
        // Metrics Endpoint (Prometheus Scrape Target)
        get("/metrics") {
            call.respond(appMicrometerRegistry.scrape())
        }
        
        get("/") {
            call.respond(KResult.ok("Hello Kairowan-Ktor Enterprise!", "Welcome to Kairowan Framework"))
        }
        
        // Protected Routes (Auth Required)
        authenticate {
            authenticatedAuthRoutes() // logout, getInfo, getRouters
            sysUserRoutes()
            sysRoleRoutes()
            sysMenuRoutes()
            sysConfigRoutes()
            sysDictRoutes()
            sysLogRoutes()
            sysDeptRoutes()
            sysPostRoutes()
            genRoutes()  // Code Generator
            monitorRoutes() // Online Users & Server Info
            sysJobRoutes() // Job Management
            webSocketApiRoutes() // WebSocket HTTP API
            sysFileRoutes() // File Management
            idempotentRoutes() // Idempotency Token
        }
        
        // WebSocket Routes
        webSocketRoutes()
    }
    
    // 8. Lifecycle
    environment.monitor.subscribe(ApplicationStarted) {
        CronManager.start()
        // Example Job (Can be moved to a Bootstrap class)
        // CronManager.addJob("TestJob", TestJob::class.java, "0/10 * * * * ?")
    }
    environment.monitor.subscribe(ApplicationStopped) {
        CronManager.shutdown()
    }
    
    // Banner
    println("""
      _  __     _                                    
     | |/ /    (_)                                   
     | ' / __ _ _ _ __ _____      ____ _ _ __  
     |  < / _` | | '__/ _ \ \ /\ / / _` | '_ \ 
     | . \ (_| | | | | (_) \ V  V / (_| | | | |
     |_|\_\__,_|_|_|  \___/ \_/\_/ \__,_|_| |_|
     
     :: Kairowan Ktor Enterprise ::  (v1.0.0)
    """.trimIndent())

    val port = environment.config.propertyOrNull("ktor.deployment.port")?.getString() ?: "8080"
    logger.info("Application initialized successfully.")
    logger.info("Swagger UI: http://localhost:$port/swagger-ui/index.html")
    logger.info("OpenAPI Spec: http://localhost:$port/swagger-ui/api.json (Import this to Apifox)")
}
