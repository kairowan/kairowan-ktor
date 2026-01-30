package com.kairowan.ktor.framework.web.service

import com.kairowan.ktor.core.async.BackgroundExecutor
import com.kairowan.ktor.core.database.DatabaseProvider
import com.kairowan.ktor.framework.web.domain.SysLoginLog
import com.kairowan.ktor.framework.web.domain.SysLoginLogs
import com.kairowan.ktor.framework.web.domain.SysOperLog
import com.kairowan.ktor.framework.web.domain.SysOperLogs
import com.kairowan.ktor.framework.web.page.KPageRequest
import com.kairowan.ktor.framework.web.page.KTableData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ktorm.dsl.*
import org.ktorm.entity.add
import org.ktorm.entity.sequenceOf
import java.time.LocalDateTime

/**
 * 操作日志服务
 * @author Kairowan
 * @date 2026-01-18
 */
class SysOperLogService(
    private val databaseProvider: DatabaseProvider,
    private val backgroundExecutor: BackgroundExecutor
) {
    private val database get() = databaseProvider.database

    /**
     * 异步记录操作日志
     */
    fun recordOperLogAsync(operLog: SysOperLog) {
        backgroundExecutor.execute {
            withContext(Dispatchers.IO) {
                database.sequenceOf(SysOperLogs).add(operLog)
            }
        }
    }

    /**
     * 查询操作日志列表
     */
    suspend fun list(page: KPageRequest? = null): KTableData = withContext(Dispatchers.IO) {
        val safePage = page?.normalized()
        val query = database.from(SysOperLogs)
            .select()
            .orderBy(SysOperLogs.operId.desc())

        if (safePage != null) {
            val offset = safePage.getOffset()
            query.limit(offset, safePage.pageSize)

            val total = database.from(SysOperLogs).select(count()).map { it.getInt(1) }.first().toLong()
            val list = query.map { SysOperLogs.createEntity(it) }

            KTableData.build(list, total)
        } else {
            val list = query.map { SysOperLogs.createEntity(it) }
            KTableData.build(list)
        }
    }

    /**
     * 清空操作日志
     */
    suspend fun clean() = withContext(Dispatchers.IO) {
        database.deleteAll(SysOperLogs)
    }
}

/**
 * 登录日志服务
 * @author Kairowan
 * @date 2026-01-18
 */
class SysLoginLogService(
    private val databaseProvider: DatabaseProvider,
    private val backgroundExecutor: BackgroundExecutor
) {
    private val database get() = databaseProvider.database

    /**
     * 异步记录登录日志
     */
    fun recordLoginLogAsync(
        userName: String,
        status: String,
        message: String,
        ipaddr: String = "",
        browser: String = "",
        os: String = ""
    ) {
        backgroundExecutor.execute {
            withContext(Dispatchers.IO) {
                val loginLog = SysLoginLog {
                    this.userName = userName
                    this.status = status
                    this.msg = message
                    this.ipaddr = ipaddr
                    this.browser = browser
                    this.os = os
                    this.loginLocation = ""
                    this.loginTime = LocalDateTime.now()
                }
                database.sequenceOf(SysLoginLogs).add(loginLog)
            }
        }
    }

    /**
     * 查询登录日志列表
     */
    suspend fun list(page: KPageRequest? = null): KTableData = withContext(Dispatchers.IO) {
        val safePage = page?.normalized()
        val query = database.from(SysLoginLogs)
            .select()
            .orderBy(SysLoginLogs.infoId.desc())

        if (safePage != null) {
            val offset = safePage.getOffset()
            query.limit(offset, safePage.pageSize)

            val total = database.from(SysLoginLogs).select(count()).map { it.getInt(1) }.first().toLong()
            val list = query.map { SysLoginLogs.createEntity(it) }

            KTableData.build(list, total)
        } else {
            val list = query.map { SysLoginLogs.createEntity(it) }
            KTableData.build(list)
        }
    }

    /**
     * 清空登录日志
     */
    suspend fun clean() = withContext(Dispatchers.IO) {
        database.deleteAll(SysLoginLogs)
    }
}
