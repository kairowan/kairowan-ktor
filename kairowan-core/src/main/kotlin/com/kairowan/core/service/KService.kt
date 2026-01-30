package com.kairowan.ktor.framework.web.service

import com.kairowan.ktor.framework.web.page.KPageRequest
import com.kairowan.ktor.framework.web.page.KTableData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.Entity
import org.ktorm.entity.add
import org.ktorm.entity.update
import org.ktorm.schema.Table
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Column
import org.ktorm.schema.IntSqlType
import org.ktorm.schema.LongSqlType

/**
 * 通用 Service 基类
 * Kairowan Generic Service (MyBatis-Plus Style)
 * 
 * @param E Entity Type
 * 
 * @author Kairowan
 * @date 2026-01-17
 */
abstract class KService<E : Entity<E>>(
    private val database: Database,
    private val table: Table<E>
) {

    /**
     * Execute suspending IO task
     */
    protected suspend fun <R> dbQuery(block: () -> R): R = withContext(Dispatchers.IO) {
        block()
    }

    suspend fun getById(id: Int): E? = dbQuery {
        val primaryKey = table.primaryKeys.singleOrNull() ?: return@dbQuery null
        val condition = when (primaryKey.sqlType) {
            is IntSqlType -> (primaryKey as Column<Int>).eq(id)
            is LongSqlType -> (primaryKey as Column<Long>).eq(id.toLong())
            else -> return@dbQuery null
        }
        database.from(table)
            .select()
            .where { condition }
            .limit(0, 1)
            .map { table.createEntity(it) }
            .firstOrNull()
    }
    
    suspend fun save(entity: E): Int = dbQuery {
        database.sequenceOf(table).add(entity)
    }
    
    suspend fun update(entity: E): Int = dbQuery {
        database.sequenceOf(table).update(entity)
    }

    suspend fun list(page: KPageRequest? = null): KTableData = dbQuery {
        val query = database.from(table).select()

        val safePage = page?.normalized()
        applyOrderBy(query, safePage)

        if (safePage != null) {
            val offset = safePage.getOffset()
            query.limit(offset, safePage.pageSize)
            
            // Simplified total count for demo
            val total = database.from(table).select(count()).map { it.getInt(1) }.first().toLong()
            val list = query.map { table.createEntity(it) }
            
            KTableData.build(list, total)
        } else {
            val list = query.map { table.createEntity(it) }
            KTableData.build(list)
        }
    }

    private fun applyOrderBy(query: Query, page: KPageRequest?) {
        val columnName = page?.orderByColumn ?: return
        val column = resolveColumn(columnName) ?: return
        if (page.isAsc == "desc") {
            query.orderBy(column.desc())
        } else {
            query.orderBy(column.asc())
        }
    }

    private fun resolveColumn(columnName: String): Column<*>? {
        val direct = table.columns.firstOrNull { it.name == columnName }
        if (direct != null) return direct
        val snake = camelToSnake(columnName)
        return table.columns.firstOrNull { it.name == snake }
    }

    private fun camelToSnake(input: String): String {
        if (input.isEmpty()) return input
        val sb = StringBuilder()
        input.forEachIndexed { index, c ->
            if (c.isUpperCase()) {
                if (index != 0) sb.append('_')
                sb.append(c.lowercaseChar())
            } else {
                sb.append(c)
            }
        }
        return sb.toString()
    }
}
