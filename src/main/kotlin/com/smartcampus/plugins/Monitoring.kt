package com.smartcampus.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.request.*
import org.slf4j.event.*

/**
 * Расширение для [Application] для настройки мониторинга и логирования HTTP запросов.
 *
 * Устанавливает плагин [CallLogging] для логирования деталей каждого входящего HTTP запроса:
 * - `level`: Устанавливает уровень логирования в [Level.INFO].
 * - `filter`: Фильтрует запросы, которые будут логироваться. В данном случае,
 *   логируются все запросы, путь которых начинается с "/".
 *
 * Это помогает отслеживать активность сервера и диагностировать проблемы.
 *
 * @receiver [Application] Экземпляр Ktor приложения.
 */
fun Application.configureMonitoring() {
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }
}
