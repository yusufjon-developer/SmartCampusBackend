package com.smartcampus.core.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

/**
 * Утилитарный класс для генерации JWT (JSON Web Token).
 * Использует конфигурацию из [JwtConfig] для создания токенов.
 *
 * @property config Конфигурация [JwtConfig], содержащая секрет, издателя, аудиторию и срок действия токена.
 * @constructor Создает экземпляр TokenUtils с заданной конфигурацией JWT.
 */
class TokenUtils(private val config: JwtConfig) {

    private val algorithm: Algorithm = Algorithm.HMAC256(config.secret)

    /**
     * Генерирует новый JWT и дату его истечения.
     *
     * Токен включает стандартные клеймы (issuer, audience, expiresAt) и кастомные клеймы:
     * - `userId`: Идентификатор пользователя.
     * - `username`: Имя пользователя.
     * - `role`: Роль пользователя.
     * - `deviceUuid` (опционально): Уникальный идентификатор устройства.
     * - `deviceType` (опционально): Тип устройства.
     *
     * @param userId Идентификатор пользователя для включения в токен.
     * @param username Имя пользователя для включения в токен.
     * @param role Роль пользователя для включения в токен.
     * @param deviceUuid Опциональный уникальный идентификатор устройства. Если предоставлен, будет добавлен в клеймы токена.
     * @param deviceType Опциональный тип устройства. Если предоставлен, будет добавлен в клеймы токена.
     * @return Пара ([Pair]), содержащая сгенерированную строку JWT и объект [Date], представляющий дату истечения срока действия токена.
     */
    fun generateToken(
        userId: Int,
        username: String,
        role: String,
        deviceUuid: String? = null,
        deviceType: String? = null
    ): Pair<String, Date> {
        val expirationDate = Date(System.currentTimeMillis() + config.validityInMs)
        val tokenBuilder = JWT.create()
            .withAudience(config.audience)
            .withIssuer(config.issuer)
            .withClaim("userId", userId)
            .withClaim("username", username)
            .withClaim("role", role)
            .withExpiresAt(expirationDate)

        if (deviceUuid != null) {
            tokenBuilder.withClaim("deviceUuid", deviceUuid)
        }
        if (deviceType != null) {
            tokenBuilder.withClaim("deviceType", deviceType)
        }

        return tokenBuilder.sign(algorithm) to expirationDate
    }
}