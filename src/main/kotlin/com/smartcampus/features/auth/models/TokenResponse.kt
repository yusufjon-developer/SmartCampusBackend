package com.smartcampus.features.auth.models

import kotlinx.serialization.Serializable

/**
 * Модель данных для ответа сервера, содержащего только токен доступа.
 * Часто используется в сценариях, где клиенту нужен только сам токен и его тип.
 *
 * @property token Сгенерированный JWT (JSON Web Token) или другой тип токена.
 * @property tokenType Тип токена, обычно "Bearer".
 */
@Serializable
data class TokenResponse(
    val token: String,
    val tokenType: String = "Bearer"
)