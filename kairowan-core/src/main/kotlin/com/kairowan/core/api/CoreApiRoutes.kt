package com.kairowan.core.api

/**
 * 核心模块接口路由常量
 *
 * @author Kairowan
 * @date 2026-01-30
 */
object CoreApiRoutes {
    object Common {
        const val ROOT = "/common"
        const val UPLOAD = "/upload"
        const val DOWNLOAD = "/download"

        const val UPLOAD_FULL = ROOT + UPLOAD
        const val DOWNLOAD_FULL = ROOT + DOWNLOAD
    }
}
