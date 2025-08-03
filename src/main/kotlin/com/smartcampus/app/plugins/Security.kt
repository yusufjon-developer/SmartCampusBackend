package com.smartcampus.app.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.smartcampus.domain.security.models.JwtConfig
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.log
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond
import org.koin.ktor.ext.inject

/**
 * Расширение для [Application] для настройки механизмов безопасности, в частности JWT аутентификации.
 *
 * Устанавливает и конфигурирует провайдер аутентификации JWT с именем "auth-jwt":
 * 1. Внедряет [JwtConfig] с помощью Koin для получения параметров JWT (секрет, издатель, аудитория, реалм).
 * 2. Устанавливает `realm` для JWT аутентификации.
 * 3. Конфигурирует `verifier` для проверки подписи и стандартных клеймов JWT:
 *    - Использует алгоритм HMAC256 с секретом из [JwtConfig].
 *    - Проверяет `audience` и `issuer` токена.
 * 4. Определяет логику `validate` для проверки кастомных клеймов в JWT:
 *    - Убеждается, что в токене присутствуют и корректны клеймы `userId` (как Int)
 *      и `username` (как непустая String).
 *    - В случае успеха валидации, возвращает [JWTPrincipal], содержащий полезную нагрузку токена.
 *    - В случае неудачи валидации, возвращает `null`, что приводит к отказу в аутентификации.
 * 5. Определяет `challenge` блок, который выполняется, когда аутентификация не удалась
 *    (например, токен не предоставлен, невалиден или просрочен).
 *    - Отправляет ответ с HTTP статусом 401 Unauthorized и сообщением.
 *
 * @receiver [Application] Экземпляр Ktor приложения.
 */
fun Application.configureSecurity() {
    val jwtConfig by inject<JwtConfig>()

    authentication {
        jwt("auth-jwt") {
            realm = jwtConfig.realm

            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtConfig.secret))
                    .withAudience(jwtConfig.audience)
                    .withIssuer(jwtConfig.issuer)
                    .build()
            )

            validate { credential ->
                if (credential.payload.getClaim("userId").asInt() != null &&
                    credential.payload.getClaim("username").asString().isNotEmpty()
                ) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }

            challenge { defaultScheme, realm ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }

    log.info("Security module configured with JWT.")
}
