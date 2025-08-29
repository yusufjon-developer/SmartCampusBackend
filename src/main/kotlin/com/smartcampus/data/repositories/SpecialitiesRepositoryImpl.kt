package com.smartcampus.data.repositories

import com.smartcampus.data.dao.SpecialitiesDao
import com.smartcampus.domain.models.SpecialityCreateRequest
import com.smartcampus.domain.models.SpecialityDto
import com.smartcampus.domain.models.SpecialityUpdateRequest
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult

class SpecialitiesRepositoryImpl(private val dao: SpecialitiesDao) {
    suspend fun listSpecialities(params: PageRequestParams): PaginatedResult<SpecialityDto> = dao.listSpecialities(params)
    suspend fun getSpecialityById(id: Int): SpecialityDto? = dao.getSpecialityById(id)
    suspend fun createSpeciality(req: SpecialityCreateRequest): SpecialityDto = dao.createSpeciality(req)
    suspend fun updateSpeciality(id: Int, req: SpecialityUpdateRequest): SpecialityDto? = dao.updateSpeciality(id, req)
    suspend fun deleteSpeciality(id: Int): Boolean = dao.deleteSpeciality(id)
}

