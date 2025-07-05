package com.smartcampus.features.auth.models

import kotlinx.serialization.Serializable

/**
 * Модель данных для запроса на вход пользователя в систему (логин).
 *
 * @property username Имя пользователя (логин).
 * @property password Пароль пользователя.
 * @property deviceUuid Опциональный уникальный идентификатор устройства, с которого происходит вход.
 *                      Может требоваться для определенных ролей или сценариев.
 * @property deviceType Опциональный тип устройства (например, "PC", "Mobile").
 *                      Может требоваться для определенных ролей или сценариев.
 */
@Serializable
data class LoginRequest(
    val username: String,
    val password: String,
    val deviceUuid: String? = null,
    val deviceType: String? = null
)