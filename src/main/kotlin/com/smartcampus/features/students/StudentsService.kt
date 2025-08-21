package com.smartcampus.features.students

import com.smartcampus.domain.models.StudentDetailsDto
import com.smartcampus.domain.models.StudentListItemDto
import com.smartcampus.domain.models.StudentUpdateRequest
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult
import com.smartcampus.domain.repositories.StudentsRepository
import org.slf4j.LoggerFactory

class StudentsService(
    private val repository: StudentsRepository
) {
    private val log = LoggerFactory.getLogger(StudentsService::class.java)

    suspend fun getStudents(params: PageRequestParams): PaginatedResult<StudentListItemDto> {
        log.debug("Fetching students with params: {}", params)
        return repository.getStudents(params)
    }

    suspend fun getStudentById(id: Int, includeSensitive: Boolean = false): StudentDetailsDto? {
        log.debug("Fetching student by id={}, includeSensitive={}", id, includeSensitive)
        return repository.getStudentById(id, includeSensitive)
    }

    suspend fun updateStudent(
        id: Int,
        request: StudentUpdateRequest,
        performingUserId: Int
    ): StudentDetailsDto? {
        log.info("Updating student id={} by user={}", id, performingUserId)
        return repository.updateStudent(id, request, performingUserId)
    }

    suspend fun deleteStudent(id: Int): Boolean {
        log.warn("Deleting student id={}", id)
        return repository.deleteStudent(id)
    }
}
