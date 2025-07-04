package com.smartcampus.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.smartcampus.core.security.JwtConfig
import com.smartcampus.core.security.TokenUtils
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.respond
import org.koin.ktor.ext.inject

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
                    // !!! ВАЖНО: блок validate не является suspend функцией.
                    // Вызов suspend функции userService.findUserById() напрямую здесь не получится.
                    // Варианты:
                    // 1. Сделать userService.findUserById не suspend (если это возможно без блокировки)
                    // 2. Использовать runBlocking - НЕ РЕКОМЕНДУЕТСЯ в validate из-за производительности.
                    // 3. Более простой подход для начала: доверять клеймам в токене, если они не критичны для безопасности на этом этапе.
                    //    Детальные проверки (активен ли юзер, его права) можно делать уже ВНУТРИ защищенных роутов.
                    // 4. Если очень нужна проверка в validate: можно сделать отдельный, НЕ suspend метод
                    //    в UserService, который выполняет быструю проверку (например, по кэшу или очень простому запросу).

                    // Для начала, давайте просто проверим наличие userId и username, как было.
                    // Дополнительную проверку активности можно добавить позже или делать в роутах.
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
