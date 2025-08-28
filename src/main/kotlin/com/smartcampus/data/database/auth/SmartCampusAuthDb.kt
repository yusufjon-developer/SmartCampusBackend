package com.smartcampus.data.database.auth

import com.smartcampus.data.database.base.DataSourceFactory
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.v1.core.DatabaseConfig
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction
import org.slf4j.LoggerFactory

class SmartCampusAuthDb(
    private val dataSourceFactory: DataSourceFactory
) {
    private val log = LoggerFactory.getLogger(SmartCampusAuthDb::class.java)

    private var dataSource: HikariDataSource? = null

    lateinit var database: Database
        private set

    fun init(dbConfig: ApplicationConfig) {
        log.info("--- Initializing SmartCampusAuthDb ---")
        try {
            dataSource = dataSourceFactory.createAndConfigureDataSource(dbConfig, "SmartCampusAuthPool")
            database = Database.Companion.connect(
                datasource = dataSource!!,
                databaseConfig = DatabaseConfig.Companion { }
            )
            log.info("SmartCampusAuthDb initialized and connected using pool '${dataSource?.poolName}'.")
        } catch (e: Exception) {
            log.error("Failed to initialize SmartCampusAuthDb: ${e.message}", e)
            throw RuntimeException("Failed to initialize SmartCampusAuthDb", e)
        }
    }

    suspend fun <T> query(block: suspend () -> T): T {
        if (!::database.isInitialized) {
            throw IllegalStateException("SmartCampusAuthDb has not been initialized. Call init() first.")
        }
        return newSuspendedTransaction(Dispatchers.IO, db = database) {
            block()
        }
    }

    fun close() {
        dataSource?.close()
        log.info("SmartCampusAuthDb connection pool '${dataSource?.poolName}' closed.")
    }
}