package com.kairowan.app.lifecycle.stages

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.kairowan.app.lifecycle.ApplicationLifecycleStage
import com.kairowan.app.lifecycle.TOKEN_BLACKLIST_PREFIX
import com.kairowan.core.framework.cache.CacheProvider
import com.kairowan.core.framework.security.LoginUser
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.jwt
import org.koin.ktor.ext.inject
import org.slf4j.Logger

/**
 * 启动阶段：认证体系配置。
 *
 * 负责 JWT 校验器与令牌黑名单校验逻辑接入。
 */
class AuthenticationConfigurationStage : ApplicationLifecycleStage {
    override fun execute(application: Application, logger: Logger) {
        val jwtSecret = application.environment.config.property("jwt.secret").getString()
        val jwtIssuer = application.environment.config.property("jwt.issuer").getString()
        val jwtAudience = application.environment.config.property("jwt.audience").getString()
        val jwtRealm = application.environment.config.property("jwt.realm").getString()
        val cache by application.inject<CacheProvider>()

        application.install(Authentication) {
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
}
