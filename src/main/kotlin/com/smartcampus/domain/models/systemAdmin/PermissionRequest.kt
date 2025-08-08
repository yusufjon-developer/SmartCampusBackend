package com.smartcampus.domain.models.systemAdmin

import kotlinx.serialization.Serializable

@Serializable
data class PermissionRequest(
    val name: String,
    val description: String
)