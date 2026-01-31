package com.kairowan.monitor.service

import com.kairowan.core.extensions.toMap
import com.kairowan.monitor.domain.SysLoginLog
import com.kairowan.monitor.domain.SysLoginLogs
import com.kairowan.monitor.domain.SysOperLog
import com.kairowan.monitor.domain.SysOperLogs
import com.kairowan.core.page.KPageRequest
import com.kairowan.core.page.KTableData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.add
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.find
import java.time.LocalDateTime

/**
 * 日志服务（统一入口）
 * @author Kairowan
 * @date 2026-01-28
 */
class SysLogService(private val database: Database) {

    /**
     * 记录操作日志
     */
    suspend fun recordOperLog(operLog: SysOperLog) = withContext(Dispatchers.IO) {
        database.sequenceOf(SysOperLogs).add(operLog)
    }

    /**
     * 查询操作日志列表
     */
    suspend fun listOperLog(page: KPageRequest? = null): KTableData = withContext(Dispatchers.IO) {
        val safePage = page?.normalized()
        val query = database.from(SysOperLogs)
            .select()
            .orderBy(SysOperLogs.operId.desc())

        if (safePage != null) {
            val offset = safePage.getOffset()
            query.limit(offset, safePage.pageSize)

            val total = database.from(SysOperLogs).select(count()).map { row -> row.getInt(1) }.first().toLong()
            val list = query.map { row -> SysOperLogs.createEntity(row) }.map { it.toMap() }

            KTableData.build(list, total)
        } else {
            val list = query.map { row -> SysOperLogs.createEntity(row) }.map { it.toMap() }
            KTableData.build(list)
        }
    }

    /**
     * 查询操作日志列表（按用户）
     */
    suspend fun listOperLogByUser(userName: String, page: KPageRequest? = null): KTableData = withContext(Dispatchers.IO) {
        val safePage = page?.normalized()
        val query = database.from(SysOperLogs)
            .select()
            .where { SysOperLogs.operName eq userName }
            .orderBy(SysOperLogs.operId.desc())

        if (safePage != null) {
            val offset = safePage.getOffset()
            query.limit(offset, safePage.pageSize)

            val total = database.from(SysOperLogs)
                .select(count())
                .where { SysOperLogs.operName eq userName }
                .map { row -> row.getInt(1) }
                .first()
                .toLong()
            val list = query.map { row -> SysOperLogs.createEntity(row) }.map { it.toMap() }

            KTableData.build(list, total)
        } else {
            val list = query.map { row -> SysOperLogs.createEntity(row) }.map { it.toMap() }
            KTableData.build(list)
        }
    }

    /**
     * 清空操作日志
     */
    suspend fun cleanOperLog() = withContext(Dispatchers.IO) {
        database.deleteAll(SysOperLogs)
    }

    /**
     * 获取操作日志详情
     */
    suspend fun getOperLogById(operId: Long): Map<String, Any?>? = withContext(Dispatchers.IO) {
        database.sequenceOf(SysOperLogs).find { it.operId eq operId }?.toMap()
    }

    /**
     * 删除操作日志
     */
    suspend fun deleteOperLog(ids: List<Long>) = withContext(Dispatchers.IO) {
        database.delete(SysOperLogs) { it.operId inList ids }
    }

    /**
     * 记录登录日志
     */
    suspend fun recordLoginLog(
        userName: String,
        status: String,
        message: String,
        ipaddr: String = "",
        browser: String = "",
        os: String = ""
    ) = withContext(Dispatchers.IO) {
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

    /**
     * 查询登录日志列表
     */
    suspend fun listLoginLog(page: KPageRequest? = null): KTableData = withContext(Dispatchers.IO) {
        val safePage = page?.normalized()
        val query = database.from(SysLoginLogs)
            .select()
            .orderBy(SysLoginLogs.infoId.desc())

        if (safePage != null) {
            val offset = safePage.getOffset()
            query.limit(offset, safePage.pageSize)

            val total = database.from(SysLoginLogs).select(count()).map { row -> row.getInt(1) }.first().toLong()
            val list = query.map { row -> SysLoginLogs.createEntity(row) }.map { it.toMap() }

            KTableData.build(list, total)
        } else {
            val list = query.map { row -> SysLoginLogs.createEntity(row) }.map { it.toMap() }
            KTableData.build(list)
        }
    }

    /**
     * 清空登录日志
     */
    suspend fun cleanLoginLog() = withContext(Dispatchers.IO) {
        database.deleteAll(SysLoginLogs)
    }

    /**
     * 删除登录日志
     */
    suspend fun deleteLoginLog(ids: List<Long>) = withContext(Dispatchers.IO) {
        database.delete(SysLoginLogs) { it.infoId inList ids }
    }

    /**
     * 获取操作日志数量（按用户）
     */
    suspend fun countOperLogByUser(userName: String): Int = withContext(Dispatchers.IO) {
        database.from(SysOperLogs)
            .select(count())
            .where { SysOperLogs.operName eq userName }
            .map { it.getInt(1) }
            .firstOrNull() ?: 0
    }

    /**
     * 获取登录日志数量（按用户）
     */
    suspend fun countLoginLogByUser(userName: String): Int = withContext(Dispatchers.IO) {
        database.from(SysLoginLogs)
            .select(count())
            .where { SysLoginLogs.userName eq userName }
            .map { it.getInt(1) }
            .firstOrNull() ?: 0
    }
}
