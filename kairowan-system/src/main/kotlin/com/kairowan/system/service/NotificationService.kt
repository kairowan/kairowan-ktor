package com.kairowan.system.service

import com.kairowan.core.page.KPageRequest
import com.kairowan.system.domain.SysNotification
import com.kairowan.system.domain.SysNotifications
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.filter
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 通知服务
 * @author Kairowan
 * @date 2026-01-29
 */
class NotificationService(private val database: Database) {
    private val timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    /**
     * 获取通知列表
     */
    suspend fun getNotificationList(
        userId: Int,
        type: String?,
        read: Boolean?,
        page: KPageRequest?
    ): com.kairowan.core.page.KTableData = withContext(Dispatchers.IO) {
        val safePage = page?.normalized()

        var query = database.from(SysNotifications)
            .select()
            .where { SysNotifications.userId eq userId }

        // 类型过滤
        if (type != null && type != "all") {
            query = query.where { SysNotifications.type eq type }
        }

        // 已读状态过滤
        if (read != null) {
            query = query.where { SysNotifications.isRead eq read }
        }

        query = query.orderBy(SysNotifications.createTime.desc())

        if (safePage != null) {
            val offset = safePage.getOffset()
            query = query.limit(offset, safePage.pageSize)

            // 计算总数
            var countQuery = database.from(SysNotifications)
                .select(count())
                .where { SysNotifications.userId eq userId }

            if (type != null && type != "all") {
                countQuery = countQuery.where { SysNotifications.type eq type }
            }
            if (read != null) {
                countQuery = countQuery.where { SysNotifications.isRead eq read }
            }

            val total = countQuery.map { it.getInt(1) }.first().toLong()

            val list = query.map { row -> buildNotificationItem(row) }
            com.kairowan.core.page.KTableData.build(list, total)
        } else {
            val list = query.map { row -> buildNotificationItem(row) }
            com.kairowan.core.page.KTableData.build(list, list.size.toLong())
        }
    }

    /**
     * 获取通知统计
     */
    suspend fun getStats(userId: Int): Map<String, Int> = withContext(Dispatchers.IO) {
        val all = database.from(SysNotifications)
            .select(count())
            .where { SysNotifications.userId eq userId }
            .map { it.getInt(1) }
            .firstOrNull() ?: 0

        val unread = database.from(SysNotifications)
            .select(count())
            .where { (SysNotifications.userId eq userId) and (SysNotifications.isRead eq false) }
            .map { it.getInt(1) }
            .firstOrNull() ?: 0

        val system = database.from(SysNotifications)
            .select(count())
            .where { (SysNotifications.userId eq userId) and (SysNotifications.type eq "system") }
            .map { it.getInt(1) }
            .firstOrNull() ?: 0

        val message = database.from(SysNotifications)
            .select(count())
            .where { (SysNotifications.userId eq userId) and (SysNotifications.type eq "message") }
            .map { it.getInt(1) }
            .firstOrNull() ?: 0

        val todo = database.from(SysNotifications)
            .select(count())
            .where { (SysNotifications.userId eq userId) and (SysNotifications.type eq "todo") }
            .map { it.getInt(1) }
            .firstOrNull() ?: 0

        mapOf(
            "all" to all,
            "unread" to unread,
            "system" to system,
            "message" to message,
            "todo" to todo
        )
    }

    /**
     * 标记已读
     */
    suspend fun markAsRead(userId: Int, ids: List<Long>): Int = withContext(Dispatchers.IO) {
        database.update(SysNotifications) {
            set(it.isRead, true)
            where {
                (it.userId eq userId) and (it.notificationId inList ids)
            }
        }
    }

    /**
     * 全部标记已读
     */
    suspend fun markAllAsRead(userId: Int): Int = withContext(Dispatchers.IO) {
        database.update(SysNotifications) {
            set(it.isRead, true)
            where {
                (it.userId eq userId) and (it.isRead eq false)
            }
        }
    }

    /**
     * 删除通知
     */
    suspend fun deleteNotifications(userId: Int, ids: List<Long>): Int = withContext(Dispatchers.IO) {
        database.delete(SysNotifications) {
            (it.userId eq userId) and (it.notificationId inList ids)
        }
    }

    /**
     * 获取未读数量
     */
    suspend fun getUnreadCount(userId: Int): Int = withContext(Dispatchers.IO) {
        database.from(SysNotifications)
            .select(count())
            .where { (SysNotifications.userId eq userId) and (SysNotifications.isRead eq false) }
            .map { it.getInt(1) }
            .firstOrNull() ?: 0
    }

    private fun buildNotificationItem(row: org.ktorm.dsl.QueryRowSet): Map<String, Any?> {
        val type = row[SysNotifications.type] ?: "system"
        val (icon, color) = defaultStyleForType(type)
        val createTime = row[SysNotifications.createTime]
        return mapOf(
            "id" to row[SysNotifications.notificationId],
            "type" to type,
            "title" to row[SysNotifications.title],
            "content" to row[SysNotifications.content],
            "time" to createTime?.format(timeFormatter),
            "read" to row[SysNotifications.isRead],
            "icon" to icon,
            "color" to color
        )
    }

    private fun defaultStyleForType(type: String): Pair<String, String> {
        return when (type) {
            "system" -> "Setting" to "#409eff"
            "message" -> "Message" to "#67c23a"
            "todo" -> "Clock" to "#e6a23c"
            else -> "Bell" to "#909399"
        }
    }
}
