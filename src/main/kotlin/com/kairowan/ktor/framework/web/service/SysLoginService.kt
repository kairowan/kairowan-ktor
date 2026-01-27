package com.kairowan.ktor.framework.web.service

import com.kairowan.ktor.common.constant.ResultCode
import com.kairowan.ktor.common.exception.ServiceException
import com.kairowan.ktor.common.utils.SecurityUtils
import com.kairowan.ktor.core.cache.CacheProvider
import com.kairowan.ktor.core.database.DatabaseProvider
import com.kairowan.ktor.framework.security.LoginUser
import com.kairowan.ktor.framework.web.domain.SysUsers
import com.kairowan.ktor.framework.web.dto.LoginBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ktorm.dsl.eq
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf

/**
 * 登录服务 (框架层 - 保留兼容性)
 * @author Kairowan
 * @date 2026-01-18
 */
class SysLoginService(
    private val databaseProvider: DatabaseProvider,
    private val tokenService: TokenService,
    private val permissionService: SysPermissionService,
    private val cache: CacheProvider
) {
    private val database get() = databaseProvider.database

    companion object {
        private const val TOKEN_BLACKLIST_PREFIX = "token:blacklist:"
        private const val TOKEN_EXPIRE_SECONDS = 86400 // 24 hours
    }

    /**
     * 用户登录
     */
    suspend fun login(loginBody: LoginBody): String = withContext(Dispatchers.IO) {
        val user = database.sequenceOf(SysUsers).find { it.userName eq loginBody.username }
            ?: throw ServiceException(ResultCode.USER_NOT_EXISTS)
        
        // 校验状态
        if (user.status == "1") {
            throw ServiceException(ResultCode.USER_DISABLED)
        }
        
        // 校验密码
        if (!SecurityUtils.matchesPassword(loginBody.password, user.password)) {
            throw ServiceException(ResultCode.USER_PASSWORD_NOT_MATCH)
        }
        
        // 获取权限
        val permissions = permissionService.getMenuPermissions(user.userId)
        val roles = permissionService.getRoleKeys(user.userId)
        
        // 创建登录用户
        val loginUser = LoginUser(
            userId = user.userId,
            username = user.userName,
            user = user,
            roles = roles,
            permissions = permissions
        )
        
        // 生成 Token
        tokenService.createToken(loginUser)
    }

    /**
     * 用户登出 (将 Token 加入黑名单)
     */
    fun logout(token: String) {
        val cleanToken = token.removePrefix("Bearer ").trim()
        val tokenId = tokenService.extractTokenId(cleanToken)
        if (tokenId != null) {
            cache.set("$TOKEN_BLACKLIST_PREFIX$tokenId", "1", TOKEN_EXPIRE_SECONDS)
        } else {
            cache.set("$TOKEN_BLACKLIST_PREFIX$cleanToken", "1", TOKEN_EXPIRE_SECONDS)
        }
    }

    /**
     * 检查 Token 是否在黑名单
     */
    fun isTokenBlacklisted(token: String): Boolean {
        val cleanToken = token.removePrefix("Bearer ").trim()
        val tokenId = tokenService.extractTokenId(cleanToken)
        return if (tokenId != null) {
            cache.exists("$TOKEN_BLACKLIST_PREFIX$tokenId")
        } else {
            cache.exists("$TOKEN_BLACKLIST_PREFIX$cleanToken")
        }
    }
}
