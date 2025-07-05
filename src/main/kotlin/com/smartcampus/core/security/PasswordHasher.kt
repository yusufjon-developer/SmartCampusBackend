package com.smartcampus.core.security

import org.mindrot.jbcrypt.BCrypt

/**
 * Утилитарный объект для хеширования паролей и проверки их соответствия хешу.
 * Использует алгоритм BCrypt для безопасного одностороннего хеширования паролей.
 */
object PasswordHasher {

    /**
     * Хеширует предоставленный пароль с использованием BCrypt.
     * Генерирует случайную соль для каждого хеша для повышения безопасности.
     *
     * @param password Пароль в открытом виде, который необходимо захешировать.
     * @return Строка, представляющая собой BCrypt хеш пароля (включая соль).
     */
    fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    /**
     * Проверяет, соответствует ли предоставленный пароль в открытом виде ранее созданному BCrypt хешу.
     *
     * @param password Пароль в открытом виде для проверки.
     * @param hashedPassword Ранее сгенерированный BCrypt хеш (который уже содержит соль).
     * @return `true`, если пароль соответствует хешу, `false` в противном случае.
     */
    fun checkPassword(password: String, hashedPassword: String): Boolean {
        return BCrypt.checkpw(password, hashedPassword)
    }
}