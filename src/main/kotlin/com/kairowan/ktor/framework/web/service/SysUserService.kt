package com.kairowan.ktor.framework.web.service

import com.kairowan.ktor.framework.web.domain.SysUser
import com.kairowan.ktor.framework.web.domain.SysUsers
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf

/**
 * 系统用户服务
 * System User Service Implementation
 * 
 * @author Kairowan
 * @date 2026-01-17
 */
class SysUserService(private val database: Database) : KService<SysUser>(database, SysUsers) {

    suspend fun getByUserId(userId: Int): SysUser? = dbQuery {
        database.sequenceOf(SysUsers).find { it.userId eq userId }
    }
}
