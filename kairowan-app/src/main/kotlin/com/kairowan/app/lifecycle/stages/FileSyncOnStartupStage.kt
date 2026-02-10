package com.kairowan.app.lifecycle.stages

import com.kairowan.app.lifecycle.ApplicationLifecycleStage
import com.kairowan.system.service.FileSyncService
import io.ktor.server.application.Application
import kotlinx.coroutines.runBlocking
import org.koin.ktor.ext.inject
import org.slf4j.Logger
import java.io.File

/**
 * 启动阶段：本地文件与数据库元数据同步。
 *
 * 支持 `syncOnce` 标记控制，避免重复全量同步。
 */
class FileSyncOnStartupStage : ApplicationLifecycleStage {
    override fun execute(application: Application, logger: Logger) {
        val config = application.environment.config
        val syncEnabled = config.propertyOrNull("file.syncOnStartup")?.getString()?.toBoolean() ?: true
        if (!syncEnabled) {
            logger.info("File sync on startup skipped (disabled by configuration)")
            return
        }

        val syncOnce = config.propertyOrNull("file.syncOnce")?.getString()?.toBoolean() ?: true
        val uploadPath = config.propertyOrNull("file.uploadPath")?.getString() ?: "uploads"
        val markerName = config.propertyOrNull("file.syncMarker")?.getString() ?: ".kairowan_file_sync.done"
        val markerFile = File(uploadPath, markerName)

        if (syncOnce && markerFile.exists()) {
            logger.info("File sync on startup skipped (syncOnce enabled and marker exists)")
            return
        }

        val uploadDir = File(uploadPath)
        if (!uploadDir.exists() || !uploadDir.isDirectory) {
            logger.warn("File sync skipped: upload path not found: {}", uploadDir.absolutePath)
            return
        }

        try {
            val fileSyncService by application.inject<FileSyncService>()
            val fileUrlPrefix = config.propertyOrNull("file.urlPrefix")?.getString() ?: "http://localhost:8080/files"
            val result = runBlocking {
                fileSyncService.syncFilesFromDisk(
                    uploadPath = uploadPath,
                    fileUrlPrefix = fileUrlPrefix,
                    defaultUserId = 1,
                    defaultUserName = "系统",
                    clearBefore = true,
                    excludeFileNames = setOf(markerName)
                )
            }
            logger.info("File sync completed: {}", result)
            if (syncOnce) {
                markerFile.parentFile?.mkdirs()
                markerFile.writeText("synced at ${java.time.LocalDateTime.now()}")
            }
        } catch (e: Exception) {
            logger.error("File sync failed", e)
        }
    }
}
