package com.kairowan.ktor.framework.web.controller

import com.kairowan.ktor.common.KResult
import com.kairowan.ktor.generator.CodeGenerator
import com.kairowan.ktor.generator.TableMetadataReader
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import org.ktorm.database.Database
import io.github.smiley4.ktorswaggerui.dsl.get
import io.github.smiley4.ktorswaggerui.dsl.post

/**
 * 代码生成器控制器
 * @author Kairowan
 * @date 2026-01-18
 */
class GenController : KController() {

    fun Route.routes() {
        val database by inject<Database>()

        route("/tool/gen") {
            // 获取所有表
            get("/tables", {
                tags = listOf("Code Generator")
                summary = "获取数据库所有表"
                securitySchemeName = "BearerAuth"
            }) {
                val reader = TableMetadataReader(database)
                val tables = reader.getAllTables()
                call.respond(KResult.ok(tables))
            }

            // 获取表结构
            get("/table/{tableName}", {
                tags = listOf("Code Generator")
                summary = "获取表结构信息"
                securitySchemeName = "BearerAuth"
            }) {
                val tableName = call.parameters["tableName"] 
                    ?: return@get call.respond(KResult.fail<Any>("表名不能为空"))
                
                val reader = TableMetadataReader(database)
                val columns = reader.readTableColumns(tableName)
                val primaryKeys = reader.getPrimaryKeys(tableName)
                
                call.respond(KResult.ok(mapOf(
                    "tableName" to tableName,
                    "columns" to columns,
                    "primaryKeys" to primaryKeys
                )))
            }

            // 预览生成代码
            get("/preview/{tableName}", {
                tags = listOf("Code Generator")
                summary = "预览生成代码"
                securitySchemeName = "BearerAuth"
            }) {
                val tableName = call.parameters["tableName"] 
                    ?: return@get call.respond(KResult.fail<Any>("表名不能为空"))
                
                val reader = TableMetadataReader(database)
                val columns = reader.readTableColumns(tableName)
                val primaryKeys = reader.getPrimaryKeys(tableName)
                val primaryKey = primaryKeys.firstOrNull() ?: "id"
                
                val generator = CodeGenerator()
                val code = generator.generateAll(tableName, columns, primaryKey)
                
                call.respond(KResult.ok(mapOf(
                    "entity" to code.entityCode,
                    "service" to code.serviceCode,
                    "controller" to code.controllerCode
                )))
            }

            // 生成代码并保存
            post("/generate/{tableName}", {
                tags = listOf("Code Generator")
                summary = "生成代码并保存到项目"
                securitySchemeName = "BearerAuth"
            }) {
                val tableName = call.parameters["tableName"] 
                    ?: return@post call.respond(KResult.fail<Any>("表名不能为空"))
                
                val reader = TableMetadataReader(database)
                val columns = reader.readTableColumns(tableName)
                val primaryKeys = reader.getPrimaryKeys(tableName)
                val primaryKey = primaryKeys.firstOrNull() ?: "id"
                
                val generator = CodeGenerator()
                val code = generator.generateAll(tableName, columns, primaryKey)
                generator.saveToFile(code)
                
                call.respond(KResult.ok<Any>(msg = "代码生成成功: ${code.className}"))
            }
        }
    }
}

fun Route.genRoutes() {
    GenController().apply { routes() }
}
