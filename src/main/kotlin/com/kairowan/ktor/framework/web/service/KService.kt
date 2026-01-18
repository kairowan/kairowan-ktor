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
        // Placeholder for generic ID lookup
        null 
    }
    
    suspend fun save(entity: E): Int = dbQuery {
        database.sequenceOf(table).add(entity)
    }
    
    suspend fun update(entity: E): Int = dbQuery {
        database.sequenceOf(table).update(entity)
    }

    suspend fun list(page: KPageRequest? = null): KTableData = dbQuery {
        val query = database.from(table).select()
        
        if (page != null) {
            val offset = page.getOffset()
            query.limit(offset, page.pageSize)
            
            // Simplified total count for demo
            val total = database.from(table).select(count()).map { it.getInt(1) }.first().toLong()
            val list = query.map { table.createEntity(it) }
            
            KTableData.build(list, total)
        } else {
            val list = query.map { table.createEntity(it) }
            KTableData.build(list)
        }
    }
}
