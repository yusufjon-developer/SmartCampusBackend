package com.smartcampus.app.plugins

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.json.Json

/**
 * Расширение для [Application] для настройки сериализации и десериализации контента
 * с использованием JSON.
 *
 * Эта функция устанавливает плагин [ContentNegotiation] в Ktor приложение,
 * что позволяет автоматически преобразовывать объекты Kotlin в JSON и обратно
 * при обработке HTTP запросов и ответов. AccessControlService качестве механизма (де)сериализации
 * используется `kotlinx.serialization.json.Json`.
 *
 * Конфигурация JSON настроена следующим образом:
 *  - `prettyPrint = true`: Включает форматирование JSON ответов с отступами,
 *    что улучшает читаемость во время разработки. AccessControlService производственной среде
 *    рекомендуется установить в `false` для уменьшения размера ответа.
 *  - `ignoreUnknownKeys = true`: Позволяет серверу игнорировать неизвестные ключи
 *    в JSON-запросах. Это делает API более устойчивым к изменениям на стороне клиента,
 *    когда клиент может отправлять новые поля, еще не поддерживаемые сервером.
 *  - `encodeDefaults = true`: Указывает, что значения по умолчанию для свойств
 *    в дата-классах должны быть включены в JSON при сериализации. Если `false`,
 *    поля со значениями по умолчанию могут быть опущены из JSON-ответа.
 */
fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                ignoreUnknownKeys = true
                encodeDefaults = true
            }
        )
    }
}
