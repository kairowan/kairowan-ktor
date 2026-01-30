package com.kairowan.system.domain

import org.ktorm.entity.Entity
import org.ktorm.schema.*
import java.time.LocalDateTime

/**
 * 文件实体
 * @author Kairowan
 * @date 2026-01-29
 */
interface SysFile : Entity<SysFile> {
    companion object : Entity.Factory<SysFile>()

    var fileId: Long
    var fileName: String         // 文件名
    var fileType: String         // 文件类型 (image/document/video/other)
    var fileSize: Long           // 文件大小（字节）
    var fileUrl: String          // 文件URL
    var thumbnailUrl: String?    // 缩略图URL
    var uploaderId: Int          // 上传者ID
    var uploaderName: String     // 上传者名称
    var createTime: LocalDateTime?
}

object SysFiles : Table<SysFile>("sys_file") {
    val fileId = long("file_id").primaryKey().bindTo { it.fileId }
    val fileName = varchar("file_name").bindTo { it.fileName }
    val fileType = varchar("file_type").bindTo { it.fileType }
    val fileSize = long("file_size").bindTo { it.fileSize }
    val fileUrl = varchar("file_url").bindTo { it.fileUrl }
    val thumbnailUrl = varchar("thumbnail_url").bindTo { it.thumbnailUrl }
    val uploaderId = int("uploader_id").bindTo { it.uploaderId }
    val uploaderName = varchar("uploader_name").bindTo { it.uploaderName }
    val createTime = datetime("create_time").bindTo { it.createTime }
}
