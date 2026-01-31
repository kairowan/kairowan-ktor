package com.kairowan.system.service

import com.kairowan.common.utils.FileUploadUtils
import com.kairowan.system.domain.SysFile
import com.kairowan.system.domain.SysFiles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ktorm.database.Database
import org.ktorm.dsl.delete
import org.ktorm.dsl.eq
import org.ktorm.dsl.greater
import org.ktorm.entity.add
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import org.slf4j.LoggerFactory

/**
 * 文件同步服务 - 用于同步物理文件到数据库
 * @author Kairowan
 * @date 2026-01-30
 */
class FileSyncService(private val database: Database) {
    private val logger = LoggerFactory.getLogger(FileSyncService::class.java)

    /**
     * 清空数据库中的所有文件记录
     */
    suspend fun clearAllFiles(): Int = withContext(Dispatchers.IO) {
        database.delete(SysFiles) { it.fileId greater 0 }
    }

    /**
     * 扫描并同步 uploads 文件夹中的文件到数据库
     * @param clearBefore 是否在同步前清空数据库（默认 true）
     */
    suspend fun syncFilesFromDisk(
        uploadPath: String,
        fileUrlPrefix: String,
        defaultUserId: Int = 1,
        defaultUserName: String = "管理员",
        clearBefore: Boolean = true,
        excludeFileNames: Set<String> = emptySet()
    ): Map<String, Any> = withContext(Dispatchers.IO) {
        val uploadDir = File(uploadPath)
        if (!uploadDir.exists() || !uploadDir.isDirectory) {
            return@withContext mapOf(
                "success" to false,
                "message" to "上传目录不存在: $uploadPath"
            )
        }

        // 清空数据库
        var clearedCount = 0
        if (clearBefore) {
            clearedCount = clearAllFiles()
            logger.info("Cleared {} existing file records from database", clearedCount)
        }

        val results = mutableListOf<String>()
        var addedCount = 0
        var skippedCount = 0
        var errorCount = 0

        // 递归扫描所有文件
        uploadDir.walkTopDown()
            .filter { it.isFile }
            .forEach { file ->
                try {
                    if (excludeFileNames.contains(file.name)) {
                        return@forEach
                    }
                    // 计算相对路径
                    val relativePath = file.relativeTo(uploadDir).path
                    val fileUrl = "$fileUrlPrefix/$relativePath"

                    // 检查数据库中是否已存在（如果不清空的话）
                    if (!clearBefore) {
                        val existing = database.sequenceOf(SysFiles)
                            .find { it.fileUrl eq fileUrl }

                        if (existing != null) {
                            skippedCount++
                            results.add("⏭️  跳过已存在: ${file.name}")
                            return@forEach
                        }
                    }

                    val fileName = file.name
                    val fileSize = file.length()
                    val fileType = FileUploadUtils.getFileType(fileName)
                    val createTime = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(file.lastModified()),
                        ZoneId.systemDefault()
                    )

                    // 插入数据库
                    val sysFile = SysFile {
                        this.fileName = fileName
                        this.fileType = fileType
                        this.fileSize = fileSize
                        this.fileUrl = fileUrl
                        this.thumbnailUrl = null
                        this.uploaderId = defaultUserId
                        this.uploaderName = defaultUserName
                        this.createTime = createTime
                    }
                    database.sequenceOf(SysFiles).add(sysFile)

                    addedCount++
                    results.add("✅ 添加: ${file.name} (${FileUploadUtils.formatFileSize(fileSize)})")
                } catch (e: Exception) {
                    errorCount++
                    results.add("❌ 错误: ${file.name} - ${e.message}")
                }
            }

        mapOf(
            "success" to true,
            "cleared" to clearedCount,
            "added" to addedCount,
            "skipped" to skippedCount,
            "errors" to errorCount,
            "total" to (addedCount + skippedCount + errorCount),
            "details" to results
        )
    }
}
