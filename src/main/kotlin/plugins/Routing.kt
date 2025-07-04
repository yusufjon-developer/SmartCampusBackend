package com.smartcampus.plugins

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

fun Application.configureRouting() {

    val authService by inject<AuthService>()

    val jwtSecret = environment.config.property("jwt.secret").getString()
    val jwtIssuer = environment.config.property("jwt.issuer").getString()
    val jwtAudience = environment.config.property("jwt.audience").getString()

    // TODO: Инжектируйте или получите экземпляр вашего UserService
    // val userService = get<UserService>() // Если используете Koin
    // val userService = UserService(DatabaseFactory.db) // Или прямая инициализация, если проще для начала

    routing {
        authRoutes(authService, this@configureRouting)

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