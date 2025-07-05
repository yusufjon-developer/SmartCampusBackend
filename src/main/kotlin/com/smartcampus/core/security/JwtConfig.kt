package com.smartcampus.core.security

/**
 * Конфигурационный класс для параметров JWT (JSON Web Token).
 * Хранит все необходимые значения для генерации и валидации токенов.
 *
 * @property secret Секретный ключ, используемый для подписи и проверки JWT.
 *                  Должен быть достаточно сложным и храниться в безопасности.
 * @property issuer Строка, идентифицирующая издателя токена (например, URL вашего сервера).
 * @property audience Строка, идентифицирующая предполагаемых получателей токена
 *                    (например, идентификатор вашего клиентского приложения или сервиса).
 * @property realm Область (realm), используемая в WWW-Authenticate заголовках при HTTP Basic/Digest аутентификации,
 *                 а также может использоваться для логического разделения групп пользователей или сервисов в контексте JWT.
 * @property validityInMs Срок действия токена в миллисекундах с момента его создания.
 */
data class JwtConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val realm: String,
    val validityInMs: Long
)