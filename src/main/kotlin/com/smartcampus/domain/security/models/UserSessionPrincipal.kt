package com.smartcampus.domain.security.models

import io.ktor.server.auth.*

data class UserSessionPrincipal(
    val userId: Int,
    val username: String,
    val roleNames: List<String>,
    val roleId: Int?
) : Principal