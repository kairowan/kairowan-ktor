package com.kairowan.generator.api

/**
 * 代码生成模块接口路由常量
 */
object GeneratorApiRoutes {
    object Gen {
        const val ROOT = "/tool/gen"
        const val TABLES = "/tables"
        const val TABLE_DETAIL = "/table/{tableName}"
        const val PREVIEW = "/preview/{tableName}"
        const val GENERATE = "/generate/{tableName}"

        const val TABLES_FULL = ROOT + TABLES
        const val TABLE_DETAIL_FULL = ROOT + TABLE_DETAIL
        const val PREVIEW_FULL = ROOT + PREVIEW
        const val GENERATE_FULL = ROOT + GENERATE
    }
}
