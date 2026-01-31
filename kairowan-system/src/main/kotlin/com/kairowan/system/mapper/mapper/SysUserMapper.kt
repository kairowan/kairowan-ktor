package com.kairowan.system.mapper

import com.kairowan.system.domain.SysUser
import org.ktorm.database.Database
import java.sql.ResultSet

/**
 * System User Mapper (Native SQL Pattern)
 * This mimics the standard DAO/Repository pattern found in most global frameworks.
 * It uses pure, standard SQL that is universally applicable.
 */
class SysUserMapper(private val database: Database) {

    /**
     * Find by ID using Native SQL
     */
    fun selectById(userId: Int): SysUser? {
        val sql = "SELECT user_id, user_name, nick_name FROM sys_user WHERE user_id = ?"
        
        return database.useConnection { conn ->
            conn.prepareStatement(sql).use { statement ->
                statement.setInt(1, userId)
                
                val rs = statement.executeQuery()
                if (rs.next()) {
                    mapRow(rs)
                } else {
                    null
                }
            }
        }
    }

    /**
     * Find List using Native SQL
     */
    fun selectList(): List<SysUser> {
        val sql = "SELECT user_id, user_name, nick_name FROM sys_user"
        val list = mutableListOf<SysUser>()
        
        database.useConnection { conn ->
            conn.prepareStatement(sql).use { statement ->
                val rs = statement.executeQuery()
                while (rs.next()) {
                    list.add(mapRow(rs))
                }
            }
        }
        return list
    }
    
    /**
     * Insert using Native SQL
     */
    fun insert(user: SysUser): Int {
        val sql = "INSERT INTO sys_user (user_name, nick_name) VALUES (?, ?)"
        
        return database.useConnection { conn ->
            conn.prepareStatement(sql).use { statement ->
                statement.setString(1, user.userName)
                statement.setString(2, user.nickName)
                statement.executeUpdate()
            }
        }
    }

    /**
     * Manual Row Mapper
     * (In a real enterprise scenario, you might use a lightweight mapper like BeanPropertyRowMapper or simple reflection utils)
     */
    private fun mapRow(rs: ResultSet): SysUser {
        // Use the companion object's invoke operator to create an instance
        val entity = SysUser()
        entity.userId = rs.getInt("user_id")
        entity.userName = rs.getString("user_name")
        entity.nickName = rs.getString("nick_name")
        return entity
    }
}
