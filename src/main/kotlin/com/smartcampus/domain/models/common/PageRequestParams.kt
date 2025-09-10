package com.smartcampus.domain.models.common

data class PageRequestParams(
    val page: Int = 1,
    val size: Int = 20,
    val sortBy: String? = null
) {
    val limit: Int get() = if (size > 0) size else 20
    val offset: Long get() = (if (page > 0) page - 1L else 0L) * limit
}