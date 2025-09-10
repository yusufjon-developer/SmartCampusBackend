package com.smartcampus.domain.models.common

import kotlinx.serialization.Serializable

@Serializable
data class PaginatedResult<T>(
    val items: List<T>,
    val totalItems: Long,
    val totalPages: Int,
    val currentPage: Int,
    val pageSize: Int,
    val sortBy: String?
)



