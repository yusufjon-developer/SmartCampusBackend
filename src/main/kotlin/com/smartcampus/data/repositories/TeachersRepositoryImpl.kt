package com.smartcampus.data.repositories

import com.smartcampus.data.dao.TeachersDao
import com.smartcampus.domain.models.TeacherDetailsDto
import com.smartcampus.domain.models.TeacherListItemDto
import com.smartcampus.domain.models.TeacherUpdateRequest
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult
import com.smartcampus.domain.repositories.TeachersRepository
import org.slf4j.LoggerFactory

class TeachersRepositoryImpl(private val dao: TeachersDao) : TeachersRepository {
    private val log = LoggerFactory.getLogger(TeachersRepositoryImpl::class.java)

    override suspend fun getTeachers(params: PageRequestParams): PaginatedResult<TeacherListItemDto> = dao.getTeachers(params)

    override suspend fun getTeacherById(id: Int, includeSensitive: Boolean): TeacherDetailsDto? = dao.getTeacherById(id, includeSensitive)

    override suspend fun updateTeacher(id: Int, request: TeacherUpdateRequest, performingUserId: Int): TeacherDetailsDto? =
        dao.updateTeacher(id, request, performingUserId)

    override suspend fun deleteTeacher(id: Int): Boolean = dao.deleteTeacher(id)
}
