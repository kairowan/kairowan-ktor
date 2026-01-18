package com.kairowan.ktor.framework.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import org.ktorm.database.Database
import org.ktorm.logging.ConsoleLogger
import org.ktorm.logging.LogLevel

object DatabaseFactory {
    fun init(config: ApplicationConfig): Database {
        val driverClass = config.property("db.driver").getString()
        val url = config.property("db.url").getString()
        val username = config.property("db.user").getString()
        val pwd = config.property("db.password").getString()

        val hikariConfig = HikariConfig().apply {
            driverClassName = driverClass
            jdbcUrl = url
            this.username = username
            this.password = pwd
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        
        val dataSource = HikariDataSource(hikariConfig)
        
        return Database.connect(
            dataSource = dataSource, 
            logger = ConsoleLogger(threshold = LogLevel.INFO)
        )
    }
}
