package com.smartcampus.app.plugins

import com.smartcampus.features.auth.AuthService
import com.smartcampus.features.auth.authRoutes
import io.ktor.server.application.Application
import io.ktor.server.application.log
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject

/**
 * Расширение для [Application] для настройки маршрутизации HTTP запросов.
 *
 * Определяет основные маршруты приложения:
 * 1. Внедряет [AuthService] с помощью Koin для использования в маршрутах аутентификации.
 * 2. Подключает маршруты аутентификации из `authRoutes`, передавая им [AuthService]
 *    и текущий экземпляр [Application].
 * 3. Определяет защищенный маршрут `/me` с использованием аутентификации "auth-jwt".
 *    - Этот маршрут доступен только для аутентифицированных пользователей с валидным JWT.
 *    - Извлекает `userId` и `username` из [JWTPrincipal] и возвращает приветственное сообщение.
 *
 * @receiver [Application] Экземпляр Ktor приложения.
 */
fun Application.configureRouting() {

    val authService by inject<AuthService>()

    // TODO: Инжектируйте или получите экземпляр вашего UserService
    // val userService = get<UserService>() // Если используете Koin
    // val userService = UserService(DatabaseFactory.db) // Или прямая инициализация, если проще для начала

    routing {
        get("/") {
            call.respondText("Hello SmartCampus!")
        }

        authRoutes(authService)

        authenticate("auth-jwt") {
            route("/me") {
                get {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    val username = principal?.payload?.getClaim("username")?.asString()
                    call.respondText("Hello, $username (ID: $userId)! You are authenticated.")
                }
            }
        }
        log.info("Routing configured.")
    }
}