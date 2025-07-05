package com.smartcampus.core.models

import kotlinx.serialization.Serializable


/**
 * Представляет общий (универсальный) ответ от API.
 * Используется для стандартных операций, где необходимо сообщить об успехе или неудаче
 * выполнения запроса и предоставить сопутствующее сообщение.
 *
 * @property success Флаг, указывающий на успешность выполнения операции.
 *                   `true` если операция завершилась успешно, `false` в противном случае.
 * @property message Сообщение, описывающее результат операции.
 *                   Может содержать информацию об успехе или детали ошибки.
 */
@Serializable
data class GenericResponse(
    val success: Boolean,
    val message: String
)