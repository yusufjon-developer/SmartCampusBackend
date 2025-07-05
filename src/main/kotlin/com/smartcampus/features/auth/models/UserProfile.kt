package com.smartcampus.features.auth.models

import kotlinx.serialization.Serializable

/**
 * Представляет профиль пользователя с основной информацией, доступной для отображения или редактирования.
 *
 * @property username Имя пользователя (логин).
 * @property email Адрес электронной почты пользователя.
 * @property fullName Полное имя пользователя.
 * @property role Роль пользователя в системе.
 * @property isActive Флаг, указывающий, активна ли учетная запись пользователя.
 *                    `true` если активна, `false` если деактивирована/заблокирована.
 */
@Serializable
data class UserProfile(
    val username: String,
    val email: String,
    val fullName: String,
    val role: String,
    val isActive: Boolean
)