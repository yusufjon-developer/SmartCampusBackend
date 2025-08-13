package com.smartcampus.app.plugins

import com.smartcampus.data.database.auth.SmartCampusAuthDb
import com.smartcampus.data.database.smartCampus.SmartCampusDb
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopping
import io.ktor.server.application.log
import org.koin.ktor.ext.inject

/**
 * Расширение для [Application] для настройки и инициализации подключений к базам данных.
 *
 * Эта функция выполняет следующие действия:
 * 1. Получает конфигурации для основной базы данных (`db_smartcampus`) и базы данных
 *    аутентификации (`db_auth`) из `application.conf`.
 * 2. Инициализирует статические объекты [SmartCampusDb] и [SmartCampusAuthDb],
 *    передавая им соответствующие конфигурации.
 * 3. Логгирует процесс инициализации. AccessControlService случае критической ошибки при инициализации
 *    любой из баз данных, выбрасывает [RuntimeException], что приведет к остановке
 *    приложения, так как работа без баз данных невозможна.
 * 4. Подписывается на событие [ApplicationStopping] для корректного закрытия
 *    соединений с базами данных при остановке приложения.
 *
 * Предполагается, что `SmartCampusDb.init()`, `SmartCampusAuthDb.init()`,
 * `SmartCampusDb.close()` и `SmartCampusAuthDb.close()` являются статическими методами
 * или методами объектов-компаньонов, отвечающими за управление жизненным циклом
 * пулов соединений с базами данных (например, HikariDataSource).
 *
 * Конфигурационные секции в `application.conf` должны выглядеть примерно так:
 * @receiver [Application] Экземпляр Ktor приложения.
 * @throws RuntimeException Если инициализация одной из баз данных завершается ошибкой.
 */
fun Application.configureDatabases() {
    val db: SmartCampusDb by inject()
    val authDb: SmartCampusAuthDb by inject()
    val log = this.log
    log.info("Configuring databases...")

    val rootConfig = this.environment.config

    try {
        log.info("Attempting to initialize SmartCampusDb...")
        val scConfig = rootConfig.config("db_smartcampus")
        db.init(scConfig)
        log.info("SmartCampusDb initialized successfully.")
    } catch (e: Exception) {
        log.error("Failed to initialize SmartCampusDb: ${e.message}", e)
        throw RuntimeException("Critical failure: SmartCampusDb initialization failed.", e)
    }

    try {
        log.info("Attempting to initialize SmartCampusAuthDb...")
        val authConfig = rootConfig.config("db_auth")
        authDb.init(authConfig)
        log.info("SmartCampusAuthDb initialized successfully.")
    } catch (e: Exception) {
        log.error("Failed to initialize SmartCampusAuthDb: ${e.message}", e)
        throw RuntimeException("Critical failure: SmartCampusAuthDb initialization failed.", e)
    }

    log.info("Database configuration process completed.")

    monitor.subscribe(ApplicationStopping) {
        log.info("Application stopping. Closing database connections...")
        try {
            db.close()
            log.info("SmartCampusDb connections closed.")
        } catch (e: Exception) {
            log.error("Error closing SmartCampusDb connections: ${e.message}", e)
        }
        try {
            authDb.close()
            log.info("SmartCampusAuthDb connections closed.")
        } catch (e: Exception) {
            log.error("Error closing SmartCampusAuthDb connections: ${e.message}", e)
        }
        log.info("Database connections closure process completed.")
    }
}