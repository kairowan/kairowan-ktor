package com.kairowan.ktor.generator

import org.ktorm.database.Database
import java.sql.ResultSet

/**
 * 代码生成器 - 数据库表元数据读取
 * @author Kairowan
 * @date 2026-01-18
 */
class TableMetadataReader(private val database: Database) {

    /**
     * 读取表的列信息
     */
    fun readTableColumns(tableName: String): List<ColumnInfo> {
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
    fun getPrimaryKeys(tableName: String): List<String> {
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
     * 获取所有表名
     */
    fun getAllTables(): List<String> {
        val tables = mutableListOf<String>()
        
        database.useConnection { conn ->
            val metaData = conn.metaData
            val rs = metaData.getTables(null, null, "%", arrayOf("TABLE"))
            
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"))
            }
        }
        
        return tables
    }
}

/**
 * 列信息
 */
data class ColumnInfo(
    val columnName: String,
    val dataType: Int,
    val typeName: String,
    val columnSize: Int,
    val nullable: Boolean,
    val remarks: String,
    val isAutoIncrement: Boolean
) {
    /**
     * 转换为 Kotlin 属性名 (下划线转驼峰)
     */
    fun toPropertyName(): String {
        return columnName.split("_")
            .mapIndexed { index, s -> 
                if (index == 0) s.lowercase() 
                else s.lowercase().replaceFirstChar { it.uppercase() }
            }
            .joinToString("")
    }

    /**
     * 转换为 Kotlin 类型
     */
    fun toKotlinType(): String {
        val baseType = when (typeName.uppercase()) {
            "INT", "INTEGER", "TINYINT", "SMALLINT" -> "Int"
            "BIGINT" -> "Long"
            "VARCHAR", "CHAR", "TEXT", "LONGTEXT" -> "String"
            "DATETIME", "TIMESTAMP" -> "LocalDateTime"
            "DATE" -> "LocalDate"
            "DECIMAL", "NUMERIC" -> "BigDecimal"
            "DOUBLE" -> "Double"
            "FLOAT" -> "Float"
            "BOOLEAN", "BIT" -> "Boolean"
            else -> "String"
        }
        
        return if (nullable && !isAutoIncrement) "$baseType?" else baseType
    }

    /**
     * 转换为 Ktorm 列类型
     */
    fun toKtormType(): String {
        return when (typeName.uppercase()) {
            "INT", "INTEGER", "TINYINT", "SMALLINT" -> "int"
            "BIGINT" -> "long"
            "VARCHAR", "CHAR", "TEXT", "LONGTEXT" -> "varchar"
            "DATETIME", "TIMESTAMP" -> "datetime"
            "DATE" -> "date"
            "DECIMAL", "NUMERIC" -> "decimal"
            "DOUBLE" -> "double"
            "FLOAT" -> "float"
            "BOOLEAN", "BIT" -> "boolean"
            else -> "varchar"
        }
    }
}
