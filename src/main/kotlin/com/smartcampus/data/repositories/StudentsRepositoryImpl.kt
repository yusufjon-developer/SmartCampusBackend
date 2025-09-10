package com.smartcampus.data.repositories

import com.smartcampus.data.dao.StudentsDao
import com.smartcampus.domain.models.StudentDetailsDto
import com.smartcampus.domain.models.StudentListItemDto
import com.smartcampus.domain.models.StudentSensitiveDto
import com.smartcampus.domain.models.StudentUpdateRequest
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult
import com.smartcampus.domain.repositories.StudentsRepository
import org.slf4j.LoggerFactory

class StudentsRepositoryImpl(private val dao: StudentsDao) : StudentsRepository {
    private val log = LoggerFactory.getLogger(StudentsRepositoryImpl::class.java)

    override suspend fun getStudents(params: PageRequestParams): PaginatedResult<StudentListItemDto> =
        dao.getStudents(params)

    override suspend fun getStudentById(id: Int): StudentDetailsDto? =
        dao.getStudentById(id)

    override suspend fun getStudentSensitiveById(id: Int): StudentSensitiveDto? =
        dao.getStudentSensitiveById(id)

    override suspend fun updateStudent(
        id: Int,
        request: StudentUpdateRequest,
        performingUserId: Int
    ): StudentDetailsDto? =
        dao.updateStudent(id, request, performingUserId)

    override suspend fun deleteStudent(id: Int): Boolean = dao.deleteStudent(id)
}
