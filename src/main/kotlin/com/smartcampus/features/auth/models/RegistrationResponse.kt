package com.smartcampus.features.auth.models

import kotlinx.serialization.Serializable


/**
 * Модель данных для ответа сервера после успешной регистрации пользователя.
 *
 * @property userId Уникальный идентификатор вновь созданного пользователя.
 * @property username Имя пользователя (логин) вновь созданного пользователя.
 * @property message Сообщение о результате операции регистрации (например, "User registered successfully.").
 */
@Serializable
data class RegistrationResponse(
    val userId: Int,
    val username: String,
    val message: String
)