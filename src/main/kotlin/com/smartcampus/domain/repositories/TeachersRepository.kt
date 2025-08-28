package com.smartcampus.domain.repositories

import com.smartcampus.domain.models.TeacherDetailsDto
import com.smartcampus.domain.models.TeacherListItemDto
import com.smartcampus.domain.models.TeacherSensitiveDto
import com.smartcampus.domain.models.TeacherUpdateRequest
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult

interface TeachersRepository {
    suspend fun getTeachers(params: PageRequestParams): PaginatedResult<TeacherListItemDto>
    suspend fun getTeacherById(id: Int): TeacherDetailsDto?
    suspend fun getTeacherSensitiveById(id: Int): TeacherSensitiveDto?
    suspend fun updateTeacher(id: Int, request: TeacherUpdateRequest, performingUserId: Int): TeacherDetailsDto?
    suspend fun deleteTeacher(id: Int): Boolean
}
