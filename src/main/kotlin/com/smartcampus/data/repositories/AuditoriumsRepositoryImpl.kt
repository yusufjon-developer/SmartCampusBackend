package com.smartcampus.data.repositories

import com.smartcampus.data.dao.AuditoriumsDao
import com.smartcampus.domain.models.AuditoriumCreateRequest
import com.smartcampus.domain.models.AuditoriumDetailsDto
import com.smartcampus.domain.models.AuditoriumListItemDto
import com.smartcampus.domain.models.AuditoriumUpdateRequest
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult
import com.smartcampus.domain.repositories.AuditoriumsRepository

class AuditoriumsRepositoryImpl(private val dao: AuditoriumsDao) : AuditoriumsRepository {
    override suspend fun getAuditoriums(params: PageRequestParams): PaginatedResult<AuditoriumListItemDto> = dao.getAuditoriums(params)
    override suspend fun getAuditoriumById(id: Int): AuditoriumDetailsDto? = dao.getAuditoriumById(id)
    override suspend fun createAuditorium(request: AuditoriumCreateRequest): AuditoriumDetailsDto = dao.createAuditorium(request)
    override suspend fun updateAuditorium(id: Int, request: AuditoriumUpdateRequest): AuditoriumDetailsDto? = dao.updateAuditorium(id, request)
    override suspend fun deleteAuditorium(id: Int): Boolean = dao.deleteAuditorium(id)
}
