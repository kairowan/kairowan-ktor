package com.kairowan.ktor.framework.web.domain

import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.long
import org.ktorm.schema.varchar
import org.ktorm.schema.datetime
import org.ktorm.entity.Entity
import java.time.LocalDateTime

/**
 * 字典类型实体
 * @author Kairowan
 * @date 2026-01-18
 */
interface SysDictType : Entity<SysDictType> {
    companion object : Entity.Factory<SysDictType>()
    
    var dictId: Long
    var dictName: String         // 字典名称
    var dictType: String         // 字典类型
    var status: String           // 状态 (0正常 1停用)
    var remark: String           // 备注
    var createTime: LocalDateTime?
}

object SysDictTypes : Table<SysDictType>("sys_dict_type") {
    val dictId = long("dict_id").primaryKey().bindTo { it.dictId }
    val dictName = varchar("dict_name").bindTo { it.dictName }
    val dictType = varchar("dict_type").bindTo { it.dictType }
    val status = varchar("status").bindTo { it.status }
    val remark = varchar("remark").bindTo { it.remark }
    val createTime = datetime("create_time").bindTo { it.createTime }
}

/**
 * 字典数据实体
 * @author Kairowan
 * @date 2026-01-18
 */
interface SysDictData : Entity<SysDictData> {
    companion object : Entity.Factory<SysDictData>()
    
    var dictCode: Long
    var dictSort: Int            // 字典排序
    var dictLabel: String        // 字典标签
    var dictValue: String        // 字典键值
    var dictType: String         // 字典类型
    var cssClass: String         // 样式属性
    var listClass: String        // 表格回显样式
    var isDefault: String        // 是否默认 (Y是 N否)
    var status: String           // 状态 (0正常 1停用)
    var remark: String           // 备注
    var createTime: LocalDateTime?
}

object SysDictDatas : Table<SysDictData>("sys_dict_data") {
    val dictCode = long("dict_code").primaryKey().bindTo { it.dictCode }
    val dictSort = int("dict_sort").bindTo { it.dictSort }
    val dictLabel = varchar("dict_label").bindTo { it.dictLabel }
    val dictValue = varchar("dict_value").bindTo { it.dictValue }
    val dictType = varchar("dict_type").bindTo { it.dictType }
    val cssClass = varchar("css_class").bindTo { it.cssClass }
    val listClass = varchar("list_class").bindTo { it.listClass }
    val isDefault = varchar("is_default").bindTo { it.isDefault }
    val status = varchar("status").bindTo { it.status }
    val remark = varchar("remark").bindTo { it.remark }
    val createTime = datetime("create_time").bindTo { it.createTime }
}
