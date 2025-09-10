package com.smartcampus.features.common

import com.smartcampus.domain.models.common.PageRequestParams
import io.ktor.server.application.*

fun ApplicationCall.getPageRequestParams(): PageRequestParams {
    val page = request.queryParameters["page"]?.toIntOrNull() ?: 1
    val size = request.queryParameters["size"]?.toIntOrNull() ?: 20
    val sortBy = request.queryParameters["sortBy"]

    return PageRequestParams(
        page = if (page < 1) 1 else page,
        size = if (size < 1) 20 else if (size > 100) 100 else size,
        sortBy = sortBy
    )
}