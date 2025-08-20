package com.smartcampus.features.auditoriums

import com.smartcampus.domain.models.AuditoriumCreateRequest
import com.smartcampus.domain.models.AuditoriumDetailsDto
import com.smartcampus.domain.models.AuditoriumListItemDto
import com.smartcampus.domain.models.AuditoriumUpdateRequest
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult
import com.smartcampus.domain.repositories.AuditoriumsRepository
import org.slf4j.LoggerFactory

class AuditoriumsService(private val repository: AuditoriumsRepository) {
    private val log = LoggerFactory.getLogger(AuditoriumsService::class.java)

    suspend fun listAuditoriums(params: PageRequestParams): PaginatedResult<AuditoriumListItemDto> {
        log.debug("Listing auditoriums params=$params")
        return repository.getAuditoriums(params)
    }

    suspend fun getAuditoriumById(id: Int): AuditoriumDetailsDto {
        log.debug("Getting auditorium id=$id")
        return repository.getAuditoriumById(id) ?: throw NoSuchElementException("Auditorium with id $id not found.")
    }

    suspend fun createAuditorium(request: AuditoriumCreateRequest): AuditoriumDetailsDto {
        log.info("Creating auditorium: $request")
        if (request.number.isBlank()) throw IllegalArgumentException("Auditorium number must not be blank")
        return repository.createAuditorium(request)
    }

    suspend fun updateAuditorium(id: Int, request: AuditoriumUpdateRequest): AuditoriumDetailsDto {
        log.info("Updating auditorium id=$id request=$request")
        return repository.updateAuditorium(id, request) ?: throw NoSuchElementException("Auditorium with id $id not found.")
    }

    suspend fun deleteAuditorium(id: Int): Boolean {
        log.info("Deleting auditorium id=$id")
        val deleted = repository.deleteAuditorium(id)
        if (!deleted) throw NoSuchElementException("Auditorium with id $id not found or could not be deleted.")
        return true
    }
}
