package com.kairowan.system.service

import com.kairowan.common.exception.ServiceException
import com.kairowan.common.utils.SecurityUtils
import com.kairowan.core.page.KPageRequest
import com.kairowan.core.page.KTableData
import com.kairowan.system.domain.SysUser
import com.kairowan.system.domain.SysUsers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.update

/**
 * 个人中心服务
 * @author Kairowan
 * @date 2026-01-29
 */
class ProfileService(
    private val database: Database
) {

    /**
     * 获取用户详细信息
     */
    suspend fun getProfileInfo(userId: Int): Map<String, Any?> = withContext(Dispatchers.IO) {
        val user = database.sequenceOf(SysUsers).find { it.userId eq userId }
            ?: throw ServiceException(message = "用户不存在")

        // TODO: 集成日志服务后获取真实数据
        val loginCount = 0
        val operationCount = 0
        val todoCount = 0

        mapOf(
            "userId" to user.userId,
            "userName" to user.userName,
            "nickName" to user.nickName,
            "email" to user.email,
            "phone" to user.phone,
            "gender" to (user.gender ?: "2"),
            "avatar" to (user.avatar ?: ""),
            "remark" to (user.remark ?: ""),
            "loginCount" to loginCount,
            "operationCount" to operationCount,
            "todoCount" to todoCount
        )
    }

    /**
     * 更新用户信息
     */
    suspend fun updateProfile(userId: Int, updates: Map<String, Any?>): Int = withContext(Dispatchers.IO) {
        val user = database.sequenceOf(SysUsers).find { it.userId eq userId }
            ?: throw ServiceException(message = "用户不存在")

        // 更新允许的字段
        updates["nickName"]?.let { user.nickName = it.toString() }
        updates["email"]?.let { user.email = it.toString() }
        updates["phone"]?.let { user.phone = it.toString() }
        updates["gender"]?.let { user.gender = it.toString() }
        updates["remark"]?.let { user.remark = it.toString() }

        database.sequenceOf(SysUsers).update(user)
    }

    /**
     * 修改密码
     */
    suspend fun updatePassword(userId: Int, oldPassword: String, newPassword: String): Int = withContext(Dispatchers.IO) {
        val user = database.sequenceOf(SysUsers).find { it.userId eq userId }
            ?: throw ServiceException(message = "用户不存在")

        // 验证旧密码
        if (!SecurityUtils.matchesPassword(oldPassword, user.password)) {
            throw ServiceException(message = "旧密码错误")
        }

        // 更新密码
        user.password = SecurityUtils.encryptPassword(newPassword)
        database.sequenceOf(SysUsers).update(user)
    }

    /**
     * 更新头像
     */
    suspend fun updateAvatar(userId: Int, avatarUrl: String): Int = withContext(Dispatchers.IO) {
        val user = database.sequenceOf(SysUsers).find { it.userId eq userId }
            ?: throw ServiceException(message = "用户不存在")

        user.avatar = avatarUrl
        database.sequenceOf(SysUsers).update(user)
    }

    /**
     * 获取操作日志
     * TODO: 需要从 monitor 模块获取
     */
    suspend fun getOperationLogs(userId: Int, page: KPageRequest?): KTableData = withContext(Dispatchers.IO) {
        val user = database.sequenceOf(SysUsers).find { it.userId eq userId }
            ?: throw ServiceException(message = "用户不存在")
        // TODO: 集成日志服务后返回真实数据
        KTableData.build(emptyList(), 0)
    }
}
