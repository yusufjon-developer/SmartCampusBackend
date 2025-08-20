package com.smartcampus.domain.repositories

import com.smartcampus.domain.models.CurriculumCreateRequest
import com.smartcampus.domain.models.CurriculumDetailsDto
import com.smartcampus.domain.models.CurriculumListItemDto
import com.smartcampus.domain.models.CurriculumUpdateRequest
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult

interface CurriculumsRepository {
    suspend fun getCurriculums(params: PageRequestParams): PaginatedResult<CurriculumListItemDto>
    suspend fun getCurriculumById(id: Int): CurriculumDetailsDto?
    suspend fun createCurriculum(request: CurriculumCreateRequest): CurriculumDetailsDto
    suspend fun updateCurriculum(id: Int, request: CurriculumUpdateRequest): CurriculumDetailsDto?
    suspend fun deleteCurriculum(id: Int): Boolean
}
