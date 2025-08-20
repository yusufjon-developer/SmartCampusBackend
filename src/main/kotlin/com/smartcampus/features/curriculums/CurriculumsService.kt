package com.smartcampus.features.curriculums

import com.smartcampus.domain.models.CurriculumCreateRequest
import com.smartcampus.domain.models.CurriculumDetailsDto
import com.smartcampus.domain.models.CurriculumListItemDto
import com.smartcampus.domain.models.CurriculumUpdateRequest
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult
import com.smartcampus.domain.repositories.CurriculumsRepository
import org.slf4j.LoggerFactory

class CurriculumsService(private val repository: CurriculumsRepository) {
    private val log = LoggerFactory.getLogger(CurriculumsService::class.java)

    suspend fun listCurriculums(params: PageRequestParams): PaginatedResult<CurriculumListItemDto> {
        log.debug("Listing curriculums")
        return repository.getCurriculums(params)
    }

    suspend fun getCurriculumById(id: Int): CurriculumDetailsDto {
        log.debug("Getting curriculum id=$id")
        return repository.getCurriculumById(id) ?: throw NoSuchElementException("Curriculum with id $id not found.")
    }

    suspend fun createCurriculum(request: CurriculumCreateRequest): CurriculumDetailsDto {
        log.info("Creating curriculum: $request")
        // basic validation
        if (request.specialityId <= 0) throw IllegalArgumentException("specialityId must be provided")
        return repository.createCurriculum(request)
    }

    suspend fun updateCurriculum(id: Int, request: CurriculumUpdateRequest): CurriculumDetailsDto {
        log.info("Updating curriculum id=$id request=$request")
        return repository.updateCurriculum(id, request) ?: throw NoSuchElementException("Curriculum with id $id not found.")
    }

    suspend fun deleteCurriculum(id: Int): Boolean {
        log.info("Deleting curriculum id=$id")
        val deleted = repository.deleteCurriculum(id)
        if (!deleted) throw NoSuchElementException("Curriculum with id $id not found or could not be deleted.")
        return true
    }

}
