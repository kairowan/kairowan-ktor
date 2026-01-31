package com.kairowan.generator.controller

import com.kairowan.common.KResult
import com.kairowan.core.controller.AuthenticatedRouteController
import com.kairowan.core.controller.KController
import com.kairowan.core.framework.security.requirePermission
import com.kairowan.generator.api.GeneratorApiRoutes
import com.kairowan.generator.core.CodeGenerator
import com.kairowan.generator.core.TableMetadataReader
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import org.ktorm.database.Database

/**
 * 代码生成器控制器
 * @author Kairowan
 * @date 2026-01-18
 */
class GenController : KController(), AuthenticatedRouteController {

    override fun register(route: Route) {
        route.routes()
    }

    private fun Route.routes() {
        val database by inject<Database>()

        route(GeneratorApiRoutes.Gen.ROOT) {
            requirePermission("tool:gen:list") {
                get(GeneratorApiRoutes.Gen.TABLES) {
                    val reader = TableMetadataReader(database)
                    val tables = reader.getAllTables()
                    call.respond(KResult.ok(tables))
                }
            }

            requirePermission("tool:gen:query") {
                get(GeneratorApiRoutes.Gen.TABLE_DETAIL) {
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
            }

            // 预览生成代码
            requirePermission("tool:gen:preview") {
                get(GeneratorApiRoutes.Gen.PREVIEW) {
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
            }

            requirePermission("tool:gen:code") {
                post(GeneratorApiRoutes.Gen.GENERATE) {
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
}
