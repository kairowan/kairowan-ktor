package com.kairowan.system.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.kairowan.core.framework.security.LoginUser
import io.ktor.server.config.*
import java.util.*

/**
 * Token 服务
 * @author Kairowan
 * @date 2026-01-17
 */
class TokenService(config: ApplicationConfig) {
    private val secret = config.property("jwt.secret").getString()
    private val issuer = config.property("jwt.issuer").getString()
    private val audience = config.property("jwt.audience").getString()
    
    private val algorithm = Algorithm.HMAC256(secret)
    private val verifier = JWT.require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()

    /**
     * 生成 Token
     */
    fun createToken(user: LoginUser): String {
        val tokenId = UUID.randomUUID().toString()
        return JWT.create()
            .withSubject("Authentication")
            .withIssuer(issuer)
            .withAudience(audience)
            .withJWTId(tokenId)
            .withClaim("userId", user.userId)
            .withClaim("username", user.username)
            .withArrayClaim("roles", user.roles.toTypedArray())
            .withArrayClaim("permissions", user.permissions.toTypedArray())
            .withExpiresAt(Date(System.currentTimeMillis() + 60000 * 60 * 24)) // 24 Hours
            .sign(algorithm)
    }

    /**
     * 验证 Token 并获取用户信息 (简单版，实际可查 Redis)
     */
    fun verifyToken(token: String): LoginUser? {
        return try {
            val decoded = verifier.verify(token)
            val userId = decoded.getClaim("userId").asInt()
            val username = decoded.getClaim("username").asString()
            val roles = decoded.getClaim("roles").asList(String::class.java)?.toSet() ?: emptySet()
            val permissions = decoded.getClaim("permissions").asList(String::class.java)?.toSet() ?: emptySet()
            LoginUser(userId, username, roles = roles, permissions = permissions)
        } catch (e: Exception) {
            null
        }
    }

    fun extractTokenId(token: String): String? {
        return try {
            JWT.decode(token).id
        } catch (e: Exception) {
            null
        }
    }
}
