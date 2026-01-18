package com.kairowan.ktor.framework.manager

import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors

/**
 * 异步任务管理器
 * 用于处理日志记录、耗时操作等，不阻塞主线程
 */
object AsyncManager {
    private val logger = LoggerFactory.getLogger(AsyncManager::class.java)
    
    // 使用单独的线程池处理后台任务，避免占用 Ktor 的 Netty 线程
    private val executor = Executors.newFixedThreadPool(10)
    private val scope = CoroutineScope(executor.asCoroutineDispatcher())

    fun execute(block: suspend () -> Unit) {
        scope.launch {
            try {
                block()
            } catch (e: Exception) {
                logger.error("Async task execution failed", e)
            }
        }
    }
    
    fun shutdown() {
        executor.shutdown()
    }
}
