package com.smartcampus.domain.repositories

import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult
import com.smartcampus.domain.models.StudentListItemDto
import com.smartcampus.domain.models.StudentDetailsDto
import com.smartcampus.domain.models.StudentCreateRequest
import com.smartcampus.domain.models.StudentUpdateRequest

interface StudentsRepository {
    suspend fun getStudents(params: PageRequestParams): PaginatedResult<StudentListItemDto>
    suspend fun getStudentById(id: Int, includeSensitive: Boolean = false): StudentDetailsDto?
    suspend fun updateStudent(id: Int, request: StudentUpdateRequest, performingUserId: Int): StudentDetailsDto?
    suspend fun deleteStudent(id: Int): Boolean
}