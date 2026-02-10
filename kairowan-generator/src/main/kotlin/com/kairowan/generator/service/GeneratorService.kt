package com.kairowan.generator.service

import com.kairowan.common.constant.CacheConstants
import com.kairowan.core.framework.cache.CacheProvider
import com.kairowan.generator.core.ColumnInfo
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.ktorm.database.Database
import org.slf4j.LoggerFactory

/**
 * 代码生成器服务
 * Generator Service with Cache
 *
 * @author Kairowan
 * @date 2026-01-28
 */
class GeneratorService(
    private val database: Database,
    private val cache: CacheProvider
) {
    private val logger = LoggerFactory.getLogger(GeneratorService::class.java)
    private val mapper = jacksonObjectMapper()

    /**
     * 获取所有表名（带缓存）
     * 缓存10分钟
     */
    fun getAllTables(): List<TableInfo> {
        val cacheKey = "gen:tables:all"

        cache.get(cacheKey)?.let { cached ->
            logger.debug("Cache hit for table list")
            return mapper.readValue(cached)
        }

        val tables = mutableListOf<TableInfo>()

        database.useConnection { conn ->
            val metaData = conn.metaData
            val rs = metaData.getTables(null, null, "%", arrayOf("TABLE"))

            while (rs.next()) {
                val tableName = rs.getString("TABLE_NAME")
                val tableComment = rs.getString("REMARKS") ?: ""

                // 过滤系统表
                if (!tableName.startsWith("sys_") && !tableName.startsWith("qrtz_")) {
                    tables.add(
                        TableInfo(
                            tableName = tableName,
                            tableComment = tableComment.ifBlank { tableName }
                        )
                    )
                }
            }
        }

        // 缓存10分钟
        cache.set(cacheKey, mapper.writeValueAsString(tables), 600)
        logger.debug("Cached table list: count=${tables.size}")

        return tables
    }

    /**
     * 获取表结构信息（带缓存）
     * 缓存1小时
     */
    fun getTableStructure(tableName: String): TableStructure? {
        val cacheKey = "gen:table:$tableName"

        cache.get(cacheKey)?.let { cached ->
            logger.debug("Cache hit for table structure: tableName=$tableName")
            return mapper.readValue(cached, TableStructure::class.java)
        }

        val columns = readTableColumns(tableName)
        if (columns.isEmpty()) {
            return null
        }

        val primaryKeys = getPrimaryKeys(tableName)
        val tableComment = getTableComment(tableName)

        val structure = TableStructure(
            tableName = tableName,
            tableComment = tableComment,
            columns = columns,
            primaryKeys = primaryKeys
        )

        // 缓存1小时
        cache.set(cacheKey, mapper.writeValueAsString(structure), CacheConstants.DEFAULT_EXPIRE_TIME)
        logger.debug("Cached table structure: tableName=$tableName")

        return structure
    }

    /**
     * 读取表的列信息
     */
    private fun readTableColumns(tableName: String): List<ColumnInfo> {
        val columns = mutableListOf<ColumnInfo>()

        database.useConnection { conn ->
            val metaData = conn.metaData
            val rs = metaData.getColumns(null, null, tableName, null)

            while (rs.next()) {
                columns.add(
                    ColumnInfo(
                        columnName = rs.getString("COLUMN_NAME"),
                        dataType = rs.getInt("DATA_TYPE"),
                        typeName = rs.getString("TYPE_NAME"),
                        columnSize = rs.getInt("COLUMN_SIZE"),
                        nullable = rs.getInt("NULLABLE") == 1,
                        remarks = rs.getString("REMARKS") ?: "",
                        isAutoIncrement = rs.getString("IS_AUTOINCREMENT") == "YES"
                    )
                )
            }
        }

        return columns
    }

    /**
     * 获取表的主键列
     */
    private fun getPrimaryKeys(tableName: String): List<String> {
        val primaryKeys = mutableListOf<String>()

        database.useConnection { conn ->
            val metaData = conn.metaData
            val rs = metaData.getPrimaryKeys(null, null, tableName)

            while (rs.next()) {
                primaryKeys.add(rs.getString("COLUMN_NAME"))
            }
        }

        return primaryKeys
    }

    /**
     * 获取表注释
     */
    private fun getTableComment(tableName: String): String {
        var comment = ""

        database.useConnection { conn ->
            val metaData = conn.metaData
            val rs = metaData.getTables(null, null, tableName, arrayOf("TABLE"))

            if (rs.next()) {
                comment = rs.getString("REMARKS") ?: tableName
            }
        }

        return comment.ifBlank { tableName }
    }

    /**
     * 清除缓存
     */
    fun clearCache(tableName: String? = null) {
        if (tableName != null) {
            cache.delete("gen:table:$tableName")
            logger.info("Cleared cache for table: $tableName")
        } else {
            cache.deleteByPattern("gen:*")
            logger.info("Cleared all generator cache")
        }
    }
}

/**
 * 表信息
 */
data class TableInfo(
    val tableName: String,
    val tableComment: String
)

/**
 * 表结构信息
 */
data class TableStructure(
    val tableName: String,
    val tableComment: String,
    val columns: List<ColumnInfo>,
    val primaryKeys: List<String>
) {
    /**
     * 获取类名 (表名转驼峰)
     */
    fun getClassName(): String {
        return tableName.split("_")
            .joinToString("") { it.lowercase().replaceFirstChar { c -> c.uppercase() } }
    }

    /**
     * 获取主键列信息
     */
    fun getPrimaryKeyColumn(): ColumnInfo? {
        return columns.firstOrNull { it.columnName in primaryKeys }
    }
}
