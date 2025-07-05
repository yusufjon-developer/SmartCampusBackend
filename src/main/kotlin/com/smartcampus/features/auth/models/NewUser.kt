package com.smartcampus.features.auth.models

import kotlinx.serialization.Serializable

/**
 * Модель данных, представляющая информацию для создания нового пользователя.
 * Используется внутренне или в запросах, где все поля обязательны.
 *
 * @property username Желаемое имя пользователя (логин). Должно быть уникальным.
 * @property email Адрес электронной почты пользователя. Должен быть уникальным.
 * @property password Пароль пользователя в открытом виде. Будет хеширован перед сохранением.
 * @property fullName Полное имя пользователя.
 * @property role Роль, назначаемая пользователю в системе.
 */
@Serializable
data class NewUser(
    val username: String,
    val email: String,
    val password: String,
    val fullName: String,
    val role: String
)