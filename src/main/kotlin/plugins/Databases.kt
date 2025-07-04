package com.smartcampus.plugins

import com.smartcampus.core.database.auth.SmartCampusAuthDb
import com.smartcampus.core.database.smartCampus.SmartCampusDb
import io.ktor.server.application.*


fun Application.configureDatabases() {
    val log = this.log
    log.info("Configuring databases...")

    val rootConfig = this.environment.config

    try {
        log.info("Attempting to initialize SmartCampusDb...")
        val scConfig = rootConfig.config("db_smartcampus")
        SmartCampusDb.init(scConfig)
        log.info("SmartCampusDb initialized successfully.")
    } catch (e: Exception) {
        log.error("Failed to initialize SmartCampusDb: ${e.message}", e)
        throw RuntimeException("Critical failure: SmartCampusDb initialization failed.", e)
    }

    try {
        log.info("Attempting to initialize SmartCampusAuthDb...")
        val authConfig = rootConfig.config("db_auth")
        SmartCampusAuthDb.init(authConfig)
        log.info("SmartCampusAuthDb initialized successfully.")
    } catch (e: Exception) {
        log.error("Failed to initialize SmartCampusAuthDb: ${e.message}", e)
        throw RuntimeException("Critical failure: SmartCampusAuthDb initialization failed.", e)
    }

    log.info("Database configuration process completed.")

    monitor.subscribe(ApplicationStopping) {
        log.info("Application stopping. Closing database connections...")
        try {
            SmartCampusDb.close()
            log.info("SmartCampusDb connections closed.")
        } catch (e: Exception) {
            log.error("Error closing SmartCampusDb connections: ${e.message}", e)
        }
        try {
            SmartCampusAuthDb.close()
            log.info("SmartCampusAuthDb connections closed.")
        } catch (e: Exception) {
            log.error("Error closing SmartCampusAuthDb connections: ${e.message}", e)
        }
        log.info("Database connections closure process completed.")
    }
}