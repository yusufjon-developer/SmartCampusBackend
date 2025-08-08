package com.smartcampus.domain.models.systemAdmin

import kotlinx.serialization.Serializable

@Serializable
data class RoleRequest(
    val name: String,
    val description: String
)
