package com.smartcampus.domain.security

import org.mindrot.jbcrypt.BCrypt

object PasswordHasher {

    fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    fun checkPassword(password: String, hashedPassword: String): Boolean {
        return BCrypt.checkpw(password, hashedPassword)
    }
}