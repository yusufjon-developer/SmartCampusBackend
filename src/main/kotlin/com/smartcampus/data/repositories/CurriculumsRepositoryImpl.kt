package com.smartcampus.data.repositories

import com.smartcampus.data.dao.CurriculumsDao
import com.smartcampus.domain.models.*
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult
import com.smartcampus.domain.repositories.CurriculumsRepository
import org.slf4j.LoggerFactory
import kotlin.math.ceil

class CurriculumsRepositoryImpl(private val dao: CurriculumsDao) : CurriculumsRepository {
    private val log = LoggerFactory.getLogger(CurriculumsRepositoryImpl::class.java)

    override suspend fun getCurriculums(params: PageRequestParams): PaginatedResult<CurriculumDto> {
        val (items, totalItems) = dao.getCurriculums(params)
        val totalPages = if (totalItems == 0L || params.limit <= 0) 0 else ceil(totalItems.toDouble() / params.limit).toInt()
        return PaginatedResult(items, totalItems, totalPages, params.page, params.limit, params.sortBy)
    }

    override suspend fun getCurriculumById(id: Int): CurriculumDto? = dao.getCurriculumById(id, includeDisciplines = true)
    override suspend fun createCurriculum(request: CurriculumCreateRequest): CurriculumDto = dao.createCurriculum(request)
    override suspend fun updateCurriculum(id: Int, request: CurriculumUpdateRequest): CurriculumDto? = dao.updateCurriculum(id, request)
    override suspend fun deleteCurriculum(id: Int): Boolean = dao.deleteCurriculum(id)
    override suspend fun addDiscipline(curriculumId: Int, req: CurriculumDisciplineCreateRequest): CurriculumDisciplineDto? = dao.addDisciplineToCurriculum(curriculumId, req)
    override suspend fun updateDiscipline(cdId: Int, req: CurriculumDisciplineUpdateRequest): CurriculumDisciplineDto? = dao.updateCurriculumDiscipline(cdId, req)
    override suspend fun deleteDiscipline(cdId: Int): Boolean = dao.deleteCurriculumDiscipline(cdId)
}
