package com.smartcampus.domain.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.smartcampus.domain.security.models.JwtConfig
import java.util.Date

class TokenUtils(private val config: JwtConfig) {

    private val algorithm: Algorithm = Algorithm.HMAC256(config.secret)

    fun generateToken(
        userId: Int,
        username: String,
        roleId: Int,
        deviceUuid: String? = null
    ): Pair<String, Date> {
        val expirationDate = Date(System.currentTimeMillis() + config.validityInMs)
        val tokenBuilder = JWT.create()
            .withAudience(config.audience)
            .withIssuer(config.issuer)
            .withClaim("userId", userId)
            .withClaim("username", username)
            .withClaim("role", roleId)
            .withExpiresAt(expirationDate)

        if (deviceUuid != null) {
            tokenBuilder.withClaim("deviceUuid", deviceUuid)
        }

        return tokenBuilder.sign(algorithm) to expirationDate
    }
}