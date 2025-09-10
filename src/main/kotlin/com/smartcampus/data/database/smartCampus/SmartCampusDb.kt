package com.smartcampus.data.database.smartCampus

import com.smartcampus.data.database.base.DataSourceFactory
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.v1.core.DatabaseConfig
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction
import org.slf4j.LoggerFactory

class SmartCampusDb(
    private val dataSourceFactory: DataSourceFactory
) {
    private val log = LoggerFactory.getLogger(SmartCampusDb::class.java)

    private var dataSource: HikariDataSource? = null

    lateinit var database: Database
        private set

    fun init(dbConfig: ApplicationConfig) {
        log.info("--- Initializing SmartCampusDb ---")
        try {
            dataSource = dataSourceFactory.createAndConfigureDataSource(dbConfig, "SmartCampusPool")
            database = Database.Companion.connect(
                datasource = dataSource!!,
                databaseConfig = DatabaseConfig.Companion {

                }
            )
            log.info("SmartCampusDb initialized and connected using pool '${dataSource?.poolName}'.")
        } catch (e: Exception) {
            log.error("Failed to initialize SmartCampusDb: ${e.message}", e)
            throw RuntimeException("Failed to initialize SmartCampusDb", e)
        }
    }

    suspend fun <T> query(block: suspend () -> T): T {
        if (!::database.isInitialized) {
            throw IllegalStateException("SmartCampusDb has not been initialized. Call init() first.")
        }
        return newSuspendedTransaction(Dispatchers.IO, db = database) {
            block()
        }
    }

    fun close() {
        dataSource?.close()
        log.info("SmartCampusDb connection pool '${dataSource?.poolName}' closed.")
    }
}