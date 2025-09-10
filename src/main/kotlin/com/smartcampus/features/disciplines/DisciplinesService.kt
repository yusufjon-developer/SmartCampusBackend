package com.smartcampus.features.disciplines

import com.smartcampus.data.dao.DisciplinesDao
import com.smartcampus.domain.models.DisciplineCreateRequest
import com.smartcampus.domain.models.DisciplineDto
import com.smartcampus.domain.models.DisciplineUpdateRequest
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult

class DisciplinesService(private val dao: DisciplinesDao) {
    suspend fun listDisciplines(params: PageRequestParams): PaginatedResult<DisciplineDto> = dao.listDisciplines(params)
    suspend fun getDiscipline(id: Int): DisciplineDto? = dao.getDisciplineById(id)
    suspend fun createDiscipline(request: DisciplineCreateRequest): DisciplineDto = dao.createDiscipline(request)
    suspend fun updateDiscipline(id: Int, request: DisciplineUpdateRequest): DisciplineDto? = dao.updateDiscipline(id, request)
    suspend fun deleteDiscipline(id: Int): Boolean = dao.deleteDiscipline(id)
}
