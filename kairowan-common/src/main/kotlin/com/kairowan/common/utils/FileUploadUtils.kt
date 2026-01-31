package com.kairowan.common.utils

import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * 文件上传工具类
 * @author Kairowan
 * @date 2026-01-30
 */
object FileUploadUtils {

    // 默认上传路径
    private const val DEFAULT_UPLOAD_PATH = "uploads"

    // 允许的图片格式
    private val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "gif", "bmp", "webp")

    // 允许的文档格式
    private val DOCUMENT_EXTENSIONS = setOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt")

    // 允许的视频格式
    private val VIDEO_EXTENSIONS = setOf("mp4", "avi", "mov", "wmv", "flv", "mkv")

    /**
     * 保存上传的文件
     * @param inputStream 文件输入流
     * @param originalFileName 原始文件名
     * @param uploadPath 上传路径（可选）
     * @return 保存后的文件路径
     */
    fun saveFile(inputStream: InputStream, originalFileName: String, uploadPath: String = DEFAULT_UPLOAD_PATH): String {
        val extension = getFileExtension(originalFileName)
        val fileName = generateFileName(extension)

        val dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
        val targetDir = Paths.get(uploadPath, dateDir)

        // 确保目录存在
        Files.createDirectories(targetDir)

        // 保存文件
        val targetPath = targetDir.resolve(fileName)
        Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING)

        // 返回相对路径
        return "$dateDir/$fileName"
    }

    /**
     * 生成唯一文件名
     */
    private fun generateFileName(extension: String): String {
        val uuid = UUID.randomUUID().toString().replace("-", "")
        val timestamp = System.currentTimeMillis()
        return "${timestamp}_${uuid}.$extension"
    }

    /**
     * 获取文件扩展名
     */
    fun getFileExtension(fileName: String): String {
        val lastDot = fileName.lastIndexOf('.')
        return if (lastDot > 0) fileName.substring(lastDot + 1).lowercase() else ""
    }

    /**
     * 获取文件类型
     */
    fun getFileType(fileName: String): String {
        val extension = getFileExtension(fileName)
        return when {
            extension in IMAGE_EXTENSIONS -> "image"
            extension in DOCUMENT_EXTENSIONS -> "document"
            extension in VIDEO_EXTENSIONS -> "video"
            else -> "other"
        }
    }

    /**
     * 验证文件大小
     * @param fileSize 文件大小（字节）
     * @param maxSize 最大大小（字节）
     */
    fun validateFileSize(fileSize: Long, maxSize: Long): Boolean {
        return fileSize <= maxSize
    }

    /**
     * 验证文件类型
     */
    fun validateFileType(fileName: String, allowedExtensions: Set<String>): Boolean {
        val extension = getFileExtension(fileName)
        return extension in allowedExtensions
    }

    /**
     * 删除文件
     */
    fun deleteFile(filePath: String, uploadPath: String = DEFAULT_UPLOAD_PATH): Boolean {
        return try {
            val path = Paths.get(uploadPath, filePath)
            Files.deleteIfExists(path)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 获取文件完整路径
     */
    fun getFullPath(relativePath: String, uploadPath: String = DEFAULT_UPLOAD_PATH): Path {
        return Paths.get(uploadPath, relativePath)
    }

    /**
     * 格式化文件大小
     */
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.2f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024))
            else -> String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024))
        }
    }
}
