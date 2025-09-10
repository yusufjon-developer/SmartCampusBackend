package com.smartcampus.domain.repositories

import com.smartcampus.domain.models.*
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult

interface CurriculumsRepository {
    suspend fun getCurriculums(params: PageRequestParams): PaginatedResult<CurriculumDto>
    suspend fun getCurriculumById(id: Int): CurriculumDto?
    suspend fun createCurriculum(request: CurriculumCreateRequest): CurriculumDto
    suspend fun updateCurriculum(id: Int, request: CurriculumUpdateRequest): CurriculumDto?
    suspend fun deleteCurriculum(id: Int): Boolean
    suspend fun addDiscipline(curriculumId: Int, req: CurriculumDisciplineCreateRequest): CurriculumDisciplineDto?
    suspend fun updateDiscipline(cdId: Int, req: CurriculumDisciplineUpdateRequest): CurriculumDisciplineDto?
    suspend fun deleteDiscipline(cdId: Int): Boolean
}
