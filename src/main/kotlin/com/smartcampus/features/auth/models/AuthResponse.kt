package com.smartcampus.features.auth.models

import kotlinx.serialization.Serializable

/**
 * Представляет ответ сервера после успешной аутентификации пользователя.
 * Содержит JWT, информацию о пользователе и срок действия токена.
 *
 * @property token Сгенерированный JWT (JSON Web Token) для аутентифицированного пользователя.
 * @property userId Уникальный идентификатор аутентифицированного пользователя.
 * @property username Имя пользователя (логин) аутентифицированного пользователя.
 * @property role Роль аутентифицированного пользователя в системе.
 */
@Serializable
data class AuthResponse(
    val token: String,
    val userId: Int,
    val username: String,
    val role: String
)
