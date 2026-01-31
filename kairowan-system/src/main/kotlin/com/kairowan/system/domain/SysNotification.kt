package com.kairowan.system.domain

import org.ktorm.entity.Entity
import org.ktorm.schema.*
import java.time.LocalDateTime

/**
 * 通知实体
 * @author Kairowan
 * @date 2026-01-29
 */
interface SysNotification : Entity<SysNotification> {
    companion object : Entity.Factory<SysNotification>()

    var notificationId: Long
    var userId: Int              // 用户ID
    var type: String             // 通知类型 (system/message/todo)
    var title: String            // 标题
    var content: String          // 内容
    var isRead: Boolean          // 是否已读
    var createTime: LocalDateTime?
}

object SysNotifications : Table<SysNotification>("sys_notification") {
    val notificationId = long("notification_id").primaryKey().bindTo { it.notificationId }
    val userId = int("user_id").bindTo { it.userId }
    val type = varchar("type").bindTo { it.type }
    val title = varchar("title").bindTo { it.title }
    val content = text("content").bindTo { it.content }
    val isRead = boolean("is_read").bindTo { it.isRead }
    val createTime = datetime("create_time").bindTo { it.createTime }
}
