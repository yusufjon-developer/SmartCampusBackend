package com.smartcampus.domain.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.smartcampus.domain.security.models.JwtConfig
import java.util.*

class TokenUtils(private val config: JwtConfig) {

    private val algorithm: Algorithm = Algorithm.HMAC256(config.secret)

    fun generateToken(
        userId: Int,
        username: String,
        roles: List<String>,
        deviceUuid: String? = null
    ): String {
        val expirationDate = Date(System.currentTimeMillis() + config.validityInMs)
        val tokenBuilder = JWT.create()
            .withAudience(config.audience)
            .withIssuer(config.issuer)
            .withClaim("userId", userId)
            .withClaim("username", username)
            .withClaim("roles", roles)
            .withExpiresAt(expirationDate)

        if (deviceUuid != null) {
            tokenBuilder.withClaim("deviceUuid", deviceUuid)
        }

        return tokenBuilder.sign(algorithm)
    }
}