package com.smartcampus.features.curriculums

import com.smartcampus.domain.models.*
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.repositories.CurriculumsRepository
import org.slf4j.LoggerFactory

class CurriculumsService(private val repo: CurriculumsRepository) {
    private val log = LoggerFactory.getLogger(CurriculumsService::class.java)

    suspend fun listCurriculums(params: PageRequestParams) = repo.getCurriculums(params)

    suspend fun getCurriculum(id: Int): CurriculumDto? = repo.getCurriculumById(id)

    suspend fun createCurriculum(request: CurriculumCreateRequest): CurriculumDto {
        // Базовая валидация
        if (request.year != null && request.year < 2000) throw IllegalArgumentException("Invalid year")
        return repo.createCurriculum(request)
    }

    suspend fun updateCurriculum(id: Int, request: CurriculumUpdateRequest): CurriculumDto? {
        return repo.updateCurriculum(id, request)
    }

    suspend fun deleteCurriculum(id: Int): Boolean {
        return repo.deleteCurriculum(id)
    }

    suspend fun addDiscipline(curriculumId: Int, req: CurriculumDisciplineCreateRequest) = repo.addDiscipline(curriculumId, req)
    suspend fun updateDiscipline(cdId: Int, req: CurriculumDisciplineUpdateRequest) = repo.updateDiscipline(cdId, req)
    suspend fun deleteDiscipline(cdId: Int) = repo.deleteDiscipline(cdId)
}
