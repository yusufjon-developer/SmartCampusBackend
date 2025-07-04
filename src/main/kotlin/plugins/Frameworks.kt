package com.smartcampus.plugins

import com.smartcampus.di.appModule
import com.smartcampus.di.authModule
import com.smartcampus.di.coreModule
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

/**
 * Расширение для [Application] для настройки фреймворка внедрения зависимостей Koin.
 *
 * Устанавливает плагин [Koin] в Ktor приложение и конфигурирует его:
 * 1. Использует [slf4jLogger] для логирования событий Koin через SLF4J.
 * 2. Загружает Koin модули:
 *    - `appModule`: Основной модуль приложения, который может зависеть от [ApplicationEnvironment].
 *    - `coreModule`: Модуль с зависимостями ядра приложения.
 *    - `authModule`: Модуль с зависимостями для функциональности аутентификации.
 *
 * @receiver [Application] Экземпляр Ktor приложения.
 */
fun Application.configureFrameworks() {
    install(Koin) {
        slf4jLogger()
        modules(appModule(this@configureFrameworks.environment), coreModule, authModule)
//        modules(module {
//            single<HelloService> {
//                HelloService {
//                    println(environment.log.info("Hello, World!"))
//                }
//            }
//        })
    }
}
