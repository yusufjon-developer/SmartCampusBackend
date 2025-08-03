package com.smartcampus.app

import com.smartcampus.app.plugins.configureDatabases
import com.smartcampus.app.plugins.configureFrameworks
import com.smartcampus.app.plugins.configureMonitoring
import com.smartcampus.app.plugins.configureRouting
import com.smartcampus.app.plugins.configureSecurity
import com.smartcampus.app.plugins.configureSerialization
import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain

/**
 * Главная функция приложения, являющаяся точкой входа.
 *
 * Эта функция делегирует запуск встроенного сервера Ktor на движке Netty
 * классу [EngineMain]. [EngineMain] считывает конфигурацию из
 * `application.conf` (или `application.yaml`) для определения порта,
 * хоста, модулей для загрузки и других параметров сервера.
 *
 * Для запуска приложения из командной строки или через IDE обычно
 * вызывается именно эта `main` функция.
 *
 * @param args Аргументы командной строки, передаваемые в [EngineMain.main].
 *             Ktor может использовать их для переопределения некоторых
 *             конфигурационных параметров.
 */
fun main(args: Array<String>) {
    // Запуск Ktor сервера на движке Netty с использованием EngineMain.
    // EngineMain автоматически найдет и вызовет функцию Application.module()
    // (или другие модули, указанные в application.conf).
    EngineMain.main(args)
}

/**
 * Основная функция модуля Ktor приложения.
 *
 * Эта функция расширения для класса [Application] вызывается Ktor при запуске
 * для конфигурации приложения. Она последовательно вызывает различные
 * функции-расширения (плагины), каждая из которых отвечает за настройку
 * определенного аспекта приложения:
 *
 *  - `configureFrameworks()`: Настраивает фреймворки, такие как Koin для внедрения зависимостей.
 *  - `configureDatabases()`: Инициализирует подключения к базам данных.
 *  - `configureSerialization()`: Настраивает сериализацию/десериализацию контента (например, JSON).
 *  - `configureMonitoring()`: Настраивает логирование запросов и другие средства мониторинга.
 *  - `configureSecurity()`: Настраивает механизмы безопасности, такие как JWT аутентификация.
 *  - `configureRouting()`: Определяет маршруты (эндпоинты) API приложения.
 *
 * Порядок вызова этих конфигурационных функций может быть важен, так как некоторые
 * конфигурации могут зависеть от других (например, маршрутизация может зависеть от
 * настроенной безопасности и внедренных сервисов).
 *
 * @receiver [Application] Экземпляр Ktor приложения, который конфигурируется.
 */
fun Application.module() {
    // Вызов конфигурационных функций для различных аспектов приложения.
    // Порядок важен для корректной инициализации.
    configureFrameworks()    // Настройка Koin DI
    configureDatabases()     // Настройка подключений к БД
    configureSerialization() // Настройка JSON и других форматов
    configureMonitoring()    // Настройка логирования запросов
    configureSecurity()      // Настройка JWT и других механизмов безопасности
    configureRouting()       // Определение маршрутов API
}
