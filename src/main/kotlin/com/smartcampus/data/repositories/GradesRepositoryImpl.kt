package com.smartcampus.data.repositories

import com.smartcampus.data.dao.GradesDao
import com.smartcampus.domain.models.GradeCreateRequest
import com.smartcampus.domain.models.GradeRecordDto
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult
import com.smartcampus.domain.repositories.GradesRepository

class GradesRepositoryImpl(private val dao: GradesDao) : GradesRepository {
    override suspend fun addGrade(request: GradeCreateRequest): GradeRecordDto = dao.addGrade(request)
    override suspend fun getGradeById(id: Int): GradeRecordDto? = dao.getGradeById(id)
    override suspend fun updateGrade(id: Int, request: GradeCreateRequest): GradeRecordDto? = dao.updateGrade(id, request)
    override suspend fun deleteGrade(id: Int): Boolean = dao.deleteGrade(id)
    override suspend fun getGradesForStudent(studentId: Int, params: PageRequestParams): PaginatedResult<GradeRecordDto> = dao.getGradesForStudent(studentId, params)
    override suspend fun getGradesForDiscipline(disciplineId: Int, params: PageRequestParams): PaginatedResult<GradeRecordDto> = dao.getGradesForDiscipline(disciplineId, params)
}
