// file: com/smartcampus/domain/repositories/AuditoriumsRepository.kt
package com.smartcampus.domain.repositories

import com.smartcampus.domain.models.AuditoriumCreateRequest
import com.smartcampus.domain.models.AuditoriumDetailsDto
import com.smartcampus.domain.models.AuditoriumListItemDto
import com.smartcampus.domain.models.AuditoriumUpdateRequest
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult

interface AuditoriumsRepository {
    suspend fun getAuditoriums(params: PageRequestParams): PaginatedResult<AuditoriumListItemDto>
    suspend fun getAuditoriumById(id: Int): AuditoriumDetailsDto?
    suspend fun createAuditorium(request: AuditoriumCreateRequest): AuditoriumDetailsDto
    suspend fun updateAuditorium(id: Int, request: AuditoriumUpdateRequest): AuditoriumDetailsDto?
    suspend fun deleteAuditorium(id: Int): Boolean
}
