package com.kairowan.system.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.kairowan.core.framework.cache.CacheProvider
import com.kairowan.system.domain.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.filter
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList

/**
 * 字典服务
 * @author Kairowan
 * @date 2026-01-18
 */
class SysDictService(
    private val database: Database,
    private val cache: CacheProvider
) {

    companion object {
        private const val CACHE_PREFIX = "sys_dict:"
        private val mapper = jacksonObjectMapper()
    }

    /**
     * 根据字典类型获取字典数据列表 (优先从缓存)
     */
    suspend fun getDictDataByType(dictType: String): List<SysDictData> {
        // 先从缓存获取
        val cached = cache.get("$CACHE_PREFIX$dictType")
        if (cached != null) {
            return try {
                val voList: List<DictDataVo> = mapper.readValue(
                    cached, 
                    mapper.typeFactory.constructCollectionType(List::class.java, DictDataVo::class.java)
                )
                voList.map { vo: DictDataVo -> 
                    SysDictData {
                        this.dictCode = vo.dictCode
                        this.dictSort = vo.dictSort
                        this.dictLabel = vo.dictLabel
                        this.dictValue = vo.dictValue
                        this.dictType = vo.dictType
                        this.cssClass = vo.cssClass
                        this.listClass = vo.listClass
                        this.isDefault = vo.isDefault
                        this.status = vo.status
                        this.remark = vo.remark
                    }
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
        
        // 缓存未命中，从数据库获取
        val list = withContext(Dispatchers.IO) {
            database.sequenceOf(SysDictDatas)
                .filter { it.dictType eq dictType }
                .filter { it.status eq "0" }
                .toList()
                .sortedBy { it.dictSort }
        }
        
        // 写入缓存
        if (list.isNotEmpty()) {
            val voList = list.map { DictDataVo.from(it) }
            cache.set("$CACHE_PREFIX$dictType", mapper.writeValueAsString(voList))
        }
        
        return list
    }

    /**
     * 获取字典类型列表
     */
    suspend fun listDictTypes(): List<SysDictType> = withContext(Dispatchers.IO) {
        database.sequenceOf(SysDictTypes)
            .filter { it.status eq "0" }
            .toList()
    }

    /**
     * 获取所有字典数据
     */
    suspend fun listDictData(dictType: String): List<SysDictData> = withContext(Dispatchers.IO) {
        database.sequenceOf(SysDictDatas)
            .filter { it.dictType eq dictType }
            .toList()
            .sortedBy { it.dictSort }
    }

    /**
     * 刷新字典缓存
     */
    fun refreshCache(dictType: String) {
        cache.delete("$CACHE_PREFIX$dictType")
    }

    /**
     * 刷新所有字典缓存
     */
    suspend fun refreshAllCache(): Int = withContext(Dispatchers.IO) {
        val types = database.sequenceOf(SysDictTypes)
            .filter { it.status eq "0" }
            .toList()
        cache.deleteByPattern("$CACHE_PREFIX*")
        types.size
    }
}

/**
 * 用于缓存序列化的 VO
 */
data class DictDataVo(
    val dictCode: Long = 0,
    val dictSort: Int = 0,
    val dictLabel: String = "",
    val dictValue: String = "",
    val dictType: String = "",
    val cssClass: String = "",
    val listClass: String = "",
    val isDefault: String = "N",
    val status: String = "0",
    val remark: String = ""
) {
    companion object {
        fun from(data: SysDictData) = DictDataVo(
            dictCode = data.dictCode,
            dictSort = data.dictSort,
            dictLabel = data.dictLabel,
            dictValue = data.dictValue,
            dictType = data.dictType,
            cssClass = data.cssClass,
            listClass = data.listClass,
            isDefault = data.isDefault,
            status = data.status,
            remark = data.remark
        )
    }
}
