package com.smartcampus.domain.repositories

import com.smartcampus.domain.models.StudentDetailsDto
import com.smartcampus.domain.models.StudentListItemDto
import com.smartcampus.domain.models.StudentSensitiveDto
import com.smartcampus.domain.models.StudentUpdateRequest
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult

interface StudentsRepository {
    suspend fun getStudents(params: PageRequestParams): PaginatedResult<StudentListItemDto>
    suspend fun getStudentById(id: Int): StudentDetailsDto?
    suspend fun getStudentSensitiveById(id: Int): StudentSensitiveDto?
    suspend fun updateStudent(id: Int, request: StudentUpdateRequest, performingUserId: Int): StudentDetailsDto?
    suspend fun deleteStudent(id: Int): Boolean
}