package com.smartcampus.domain.repositories

import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult
import com.smartcampus.domain.models.GradeCreateRequest
import com.smartcampus.domain.models.GradeRecordDto

interface GradesRepository {
    suspend fun addGrade(request: GradeCreateRequest): GradeRecordDto
    suspend fun getGradeById(id: Int): GradeRecordDto?
    suspend fun updateGrade(id: Int, request: GradeCreateRequest): GradeRecordDto?
    suspend fun deleteGrade(id: Int): Boolean
    suspend fun getGradesForStudent(studentId: Int, params: PageRequestParams): PaginatedResult<GradeRecordDto>
    suspend fun getGradesForDiscipline(disciplineId: Int, params: PageRequestParams): PaginatedResult<GradeRecordDto>
}
