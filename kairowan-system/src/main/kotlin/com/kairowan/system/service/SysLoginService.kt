package com.kairowan.system.service

import com.kairowan.common.constant.ResultCode
import com.kairowan.common.exception.ServiceException
import com.kairowan.common.utils.SecurityUtils
import com.kairowan.core.framework.cache.CacheProvider
import com.kairowan.core.framework.security.LoginUser
import com.kairowan.system.domain.SysUsers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf

/**
 * 登录服务 (框架层 - 保留兼容性)
 * @author Kairowan
 * @date 2026-01-18
 */
class SysLoginService(
    private val database: Database,
    private val tokenService: TokenService,
    private val permissionService: SysPermissionService,
    private val cache: CacheProvider
) {

    companion object {
        private const val TOKEN_BLACKLIST_PREFIX = "token:blacklist:"
        private const val TOKEN_EXPIRE_SECONDS = 86400 // 24 hours
    }

    /**
     * 用户登录
     */
    suspend fun login(username: String, password: String): String = withContext(Dispatchers.IO) {
        val user = database.sequenceOf(SysUsers).find { it.userName eq username }
            ?: throw ServiceException(ResultCode.USER_NOT_EXISTS)
        
        // 校验状态
        if (user.status == "1") {
            throw ServiceException(ResultCode.USER_DISABLED)
        }
        
        // 校验密码
        if (!SecurityUtils.matchesPassword(password, user.password)) {
            throw ServiceException(ResultCode.USER_PASSWORD_NOT_MATCH)
        }
        
        val permissions = permissionService.getMenuPermissions(user.userId)
        val roles = permissionService.getRoleKeys(user.userId)
        
        val loginUser = LoginUser(
            userId = user.userId,
            username = user.userName,
            user = user,
            roles = roles,
            permissions = permissions
        )
        
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
