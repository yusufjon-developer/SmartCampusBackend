package com.smartcampus.features.teachers

import com.smartcampus.domain.models.TeacherDetailsDto
import com.smartcampus.domain.models.TeacherListItemDto
import com.smartcampus.domain.models.TeacherSensitiveDto
import com.smartcampus.domain.models.TeacherUpdateRequest
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult
import com.smartcampus.domain.repositories.TeachersRepository
import org.slf4j.LoggerFactory

class TeachersService(private val repository: TeachersRepository) {
    private val log = LoggerFactory.getLogger(TeachersService::class.java)

    suspend fun getTeachers(params: PageRequestParams): PaginatedResult<TeacherListItemDto> =
        repository.getTeachers(params)

    suspend fun getTeacherById(id: Int): TeacherDetailsDto? =
        repository.getTeacherById(id)

    suspend fun getTeacherSensitiveInfo(id: Int): TeacherSensitiveDto? =
        repository.getTeacherSensitiveById(id)

    suspend fun updateTeacher(id: Int, request: TeacherUpdateRequest, performingUserId: Int): TeacherDetailsDto? {
        log.info("Updating teacher id=$id by user=$performingUserId")
        return repository.updateTeacher(id, request, performingUserId)
    }

    suspend fun deleteTeacher(id: Int): Boolean = repository.deleteTeacher(id)
}
