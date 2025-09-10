package com.smartcampus.features.specialities

import com.smartcampus.data.repositories.SpecialitiesRepositoryImpl
import com.smartcampus.domain.models.SpecialityCreateRequest
import com.smartcampus.domain.models.SpecialityDto
import com.smartcampus.domain.models.SpecialityUpdateRequest
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult

class SpecialitiesService(private val repo: SpecialitiesRepositoryImpl) {
    suspend fun listSpecialities(params: PageRequestParams): PaginatedResult<SpecialityDto> = repo.listSpecialities(params)
    suspend fun getSpeciality(id: Int): SpecialityDto? = repo.getSpecialityById(id)
    suspend fun createSpeciality(req: SpecialityCreateRequest): SpecialityDto = repo.createSpeciality(req)
    suspend fun updateSpeciality(id: Int, req: SpecialityUpdateRequest): SpecialityDto? = repo.updateSpeciality(id, req)
    suspend fun deleteSpeciality(id: Int): Boolean = repo.deleteSpeciality(id)
}
