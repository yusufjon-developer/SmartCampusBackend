package com.smartcampus.app.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.smartcampus.data.dao.SystemAdminDao
import com.smartcampus.domain.security.models.JwtConfig
import com.smartcampus.domain.security.models.UserSessionPrincipal
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond
import org.koin.ktor.ext.inject

fun Application.configureSecurity() {
    val jwtConfig by inject<JwtConfig>()
    val systemAdminDao by inject<SystemAdminDao>()

    install(Authentication) {
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
                val userId = credential.payload.getClaim("userId").asInt()
                val username = credential.payload.getClaim("username").asString()
                val roleNameListFromToken =
                    credential.payload.getClaim("roles").asList(String::class.java) ?: emptyList()

                if (userId == null || username == null) {
                    this@configureSecurity.log.warn("JWT Validation: 'userId' or 'username' claim missing or null in token.")
                    return@validate null
                }

                var effectiveRoleId: Int? = null
                if (roleNameListFromToken.isNotEmpty()) {
                    val firstRoleName = roleNameListFromToken.first()
                    try {
                        val role =
                            systemAdminDao.getRoleByName(firstRoleName)
                        effectiveRoleId = role?.id
                        if (role == null) {
                            this@configureSecurity.log.warn("JWT Validation: Role with name '$firstRoleName' (from token) not found in DB for user '$username' (ID: $userId).")
                        }
                    } catch (e: Exception) {
                        this@configureSecurity.log.error(
                            "JWT Validation: DB error fetching role '$firstRoleName' for user '$username' (ID: $userId).",
                            e
                        )
                        return@validate null
                    }
                }

                try {
                    val userInfoTriple =
                        systemAdminDao.getUserInfoById(userId)
                    if (userInfoTriple == null) {
                        this@configureSecurity.log.warn("JWT Validation: User with ID $userId (username: '$username' from token) not found in database.")
                        return@validate null
                    }
                } catch (e: Exception) {
                    this@configureSecurity.log.error(
                        "JWT Validation: Database error checking user existence for ID $userId.",
                        e
                    )
                    return@validate null
                }

                UserSessionPrincipal(
                    userId = userId,
                    username = username,
                    roleNames = roleNameListFromToken,
                    roleId = effectiveRoleId
                )
            }
            challenge { defaultScheme, realm ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf(
                        "error" to "Token is not valid or has expired.",
                        "message" to "Please provide a valid token.",
                    ),
                )
            }
        }

        jwt("auth-jwt-admin") {
            val adminRole = "SystemAdmin"

            realm = jwtConfig.realm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtConfig.secret))
                    .withAudience(jwtConfig.audience)
                    .withIssuer(jwtConfig.issuer)
                    .build()
            )
            validate { credential ->
                val userId = credential.payload.getClaim("userId")?.asInt()
                val username = credential.payload.getClaim("username")?.asString()

                // Получаем список ролей из клейма "roles"
                val userRoles = try {
                    credential.payload.getClaim("roles")?.asList(String::class.java)
                        ?: emptyList()
                } catch (e: Exception) {
                    this@configureSecurity.log.warn("Failed to parse 'roles' claim as List<String> for user '$username' (ID: $userId). Error: ${e.message}")
                    emptyList<String>() // Если клейм не является списком строк, считаем, что ролей нет
                }

                if (userId != null && !username.isNullOrEmpty()) {
                    // Проверяем, содержит ли список ролей пользователя необходимую роль администратора
                    if (userRoles.contains(adminRole)) {
                        JWTPrincipal(credential.payload)
                    } else {
                        this@configureSecurity.log.warn(
                            "auth-jwt-admin validation failed for user '$username' (ID: $userId): " +
                                    "'$adminRole' role missing. User roles: $userRoles"
                        )
                        null // Роль администратора не найдена
                    }
                } else {
                    this@configureSecurity.log.warn("auth-jwt-admin validation failed: userId or username missing/invalid.")
                    null // Базовые клеймы не прошли
                }
            }
            challenge { _, _ ->
                // Ответ при неудачной аутентификации/авторизации администратора
                // Можно сделать его отличным от обычного Unauthorized, если нужно,
                // но для простоты Forbidden (403) здесь более уместен,
                // так как пользователь может быть аутентифицирован (валидный токен), но не авторизован (нет прав админа).
                // Однако, если validate возвращает null, Ktor обычно отвечает 401.
                // Чтобы получить 403, если пользователь аутентифицирован, но не имеет роли,
                // можно было бы кидать исключение в validate, а не возвращать null,
                // и обрабатывать его в StatusPages.
                // Либо, если validate вернул null из-за отсутствия роли, но токен валиден,
                // сам Ktor все равно вернет 401.
                // Для простоты оставим 401, но с сообщением, намекающим на права.
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Access denied or token invalid/expired. Administrator rights required.")
                )
            }
        }
    }

    log.info("Security module configured with JWT providers: 'auth-jwt' and 'auth-jwt-admin'.")
}

