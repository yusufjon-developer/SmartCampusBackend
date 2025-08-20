package com.smartcampus.data.repositories

import com.smartcampus.data.dao.CurriculumsDao
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult
import com.smartcampus.domain.models.CurriculumCreateRequest
import com.smartcampus.domain.models.CurriculumDetailsDto
import com.smartcampus.domain.models.CurriculumListItemDto
import com.smartcampus.domain.models.CurriculumUpdateRequest
import com.smartcampus.domain.repositories.CurriculumsRepository

class CurriculumsRepositoryImpl(private val dao: CurriculumsDao) : CurriculumsRepository {
    override suspend fun getCurriculums(params: PageRequestParams): PaginatedResult<CurriculumListItemDto> =
        dao.getCurriculums(params)

    override suspend fun getCurriculumById(id: Int): CurriculumDetailsDto? =
        dao.getCurriculumById(id)

    override suspend fun createCurriculum(request: CurriculumCreateRequest): CurriculumDetailsDto =
        dao.createCurriculum(request)

    override suspend fun updateCurriculum(
        id: Int,
        request: CurriculumUpdateRequest
    ): CurriculumDetailsDto? = dao.updateCurriculum(id, request)

    override suspend fun deleteCurriculum(id: Int): Boolean = dao.deleteCurriculum(id)
}
