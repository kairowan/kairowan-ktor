package com.kairowan.system.domain

import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar
import org.ktorm.schema.datetime
import org.ktorm.entity.Entity
import java.time.LocalDateTime

/**
 * 系统配置实体
 * @author Kairowan
 * @date 2026-01-18
 */
interface SysConfig : Entity<SysConfig> {
    companion object : Entity.Factory<SysConfig>()
    
    var configId: Int
    var configName: String       // 参数名称
    var configKey: String        // 参数键名
    var configValue: String      // 参数键值
    var configType: String       // 系统内置 (Y是 N否)
    var remark: String           // 备注
    var createTime: LocalDateTime?
}

object SysConfigs : Table<SysConfig>("sys_config") {
    val configId = int("config_id").primaryKey().bindTo { it.configId }
    val configName = varchar("config_name").bindTo { it.configName }
    val configKey = varchar("config_key").bindTo { it.configKey }
    val configValue = varchar("config_value").bindTo { it.configValue }
    val configType = varchar("config_type").bindTo { it.configType }
    val remark = varchar("remark").bindTo { it.remark }
    val createTime = datetime("create_time").bindTo { it.createTime }
}
