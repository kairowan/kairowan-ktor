package com.kairowan.system.service

import com.kairowan.common.exception.ServiceException
import com.kairowan.core.extensions.toMap
import com.kairowan.core.page.KPageRequest
import com.kairowan.core.page.KTableData
import com.kairowan.system.domain.SysFile
import com.kairowan.system.domain.SysFiles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.add
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.slf4j.LoggerFactory

/**
 * 文件管理服务
 * @author Kairowan
 * @date 2026-01-29
 */
class FileService(private val database: Database) {
    private val logger = LoggerFactory.getLogger(FileService::class.java)

    /**
     * 获取文件列表
     */
    suspend fun getFileList(
        type: String?,
        keyword: String?,
        page: KPageRequest?
    ): KTableData = withContext(Dispatchers.IO) {
        val safePage = page?.normalized()

        logger.debug("FileService.getFileList - type: {}, keyword: {}, page: {}", type, keyword, safePage)

        var query = database.from(SysFiles)
            .select()

        // 类型过滤
        if (type != null && type != "all") {
            query = query.where { SysFiles.fileType eq type }
            logger.debug("Added type filter: {}", type)
        }

        // 关键词搜索
        if (!keyword.isNullOrBlank()) {
            query = query.where { SysFiles.fileName like "%$keyword%" }
            logger.debug("Added keyword filter: {}", keyword)
        }

        query = query.orderBy(SysFiles.createTime.desc())

        if (safePage != null) {
            val offset = safePage.getOffset()
            query = query.limit(offset, safePage.pageSize)

            // 计算总数
            var countQuery = database.from(SysFiles)
                .select(count())

            if (type != null && type != "all") {
                countQuery = countQuery.where { SysFiles.fileType eq type }
            }
            if (!keyword.isNullOrBlank()) {
                countQuery = countQuery.where { SysFiles.fileName like "%$keyword%" }
            }

            val total = countQuery.map { it.getInt(1) }.first().toLong()
            logger.debug("Total count: {}", total)

            val list = query.map { row ->
                val sizeBytes = row[SysFiles.fileSize] ?: 0L
                val createTime = row[SysFiles.createTime]
                mapOf(
                    "id" to row[SysFiles.fileId],
                    "name" to row[SysFiles.fileName],
                    "type" to getFileExtension(row[SysFiles.fileName] ?: ""),
                    "size" to formatFileSize(sizeBytes),
                    "sizeBytes" to sizeBytes,
                    "uploadTime" to createTime?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    "uploader" to row[SysFiles.uploaderName],
                    "url" to row[SysFiles.fileUrl],
                    "thumbnail" to row[SysFiles.thumbnailUrl]
                )
            }

            KTableData.build(list, total)
        } else {
            val list = query.map { row ->
                val sizeBytes = row[SysFiles.fileSize] ?: 0L
                val createTime = row[SysFiles.createTime]
                mapOf(
                    "id" to row[SysFiles.fileId],
                    "name" to row[SysFiles.fileName],
                    "type" to getFileExtension(row[SysFiles.fileName] ?: ""),
                    "size" to formatFileSize(sizeBytes),
                    "sizeBytes" to sizeBytes,
                    "uploadTime" to createTime?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    "uploader" to row[SysFiles.uploaderName],
                    "url" to row[SysFiles.fileUrl],
                    "thumbnail" to row[SysFiles.thumbnailUrl]
                )
            }
            KTableData.build(list)
        }
    }

    /**
     * 获取文件统计
     */
    suspend fun getStats(): Map<String, Int> = withContext(Dispatchers.IO) {
        val all = database.from(SysFiles)
            .select(count())
            .map { it.getInt(1) }
            .firstOrNull() ?: 0

        val image = database.from(SysFiles)
            .select(count())
            .where { SysFiles.fileType eq "image" }
            .map { it.getInt(1) }
            .firstOrNull() ?: 0

        val document = database.from(SysFiles)
            .select(count())
            .where { SysFiles.fileType eq "document" }
            .map { it.getInt(1) }
            .firstOrNull() ?: 0

        val video = database.from(SysFiles)
            .select(count())
            .where { SysFiles.fileType eq "video" }
            .map { it.getInt(1) }
            .firstOrNull() ?: 0

        mapOf(
            "all" to all,
            "image" to image,
            "document" to document,
            "video" to video
        )
    }

    /**
     * 上传文件
     */
    suspend fun uploadFile(
        fileName: String,
        fileType: String,
        fileSize: Long,
        fileUrl: String,
        thumbnailUrl: String?,
        uploaderId: Int,
        uploaderName: String
    ): Long = withContext(Dispatchers.IO) {
        logger.info("Uploading file: name={}, type={}, size={}", fileName, fileType, fileSize)
        val file = SysFile {
            this.fileName = fileName
            this.fileType = fileType
            this.fileSize = fileSize
            this.fileUrl = fileUrl
            this.thumbnailUrl = thumbnailUrl
            this.uploaderId = uploaderId
            this.uploaderName = uploaderName
            this.createTime = LocalDateTime.now()
        }
        database.sequenceOf(SysFiles).add(file)
        logger.info("File uploaded with ID: {}", file.fileId)
        file.fileId
    }

    /**
     * 获取文件信息
     */
    suspend fun getFileById(fileId: Long): SysFile? = withContext(Dispatchers.IO) {
        database.sequenceOf(SysFiles).find { it.fileId eq fileId }
    }

    /**
     * 删除文件
     */
    suspend fun deleteFile(fileId: Long): Int = withContext(Dispatchers.IO) {
        database.delete(SysFiles) { it.fileId eq fileId }
    }

    /**
     * 批量删除文件
     */
    suspend fun batchDeleteFiles(ids: List<Long>): Int = withContext(Dispatchers.IO) {
        database.delete(SysFiles) { it.fileId inList ids }
    }

    /**
     * 格式化文件大小
     */
    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.2f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024))
            else -> String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024))
        }
    }

    /**
     * 获取文件扩展名
     */
    private fun getFileExtension(fileName: String): String {
        val lastDot = fileName.lastIndexOf('.')
        return if (lastDot > 0) fileName.substring(lastDot + 1).lowercase() else ""
    }
}
