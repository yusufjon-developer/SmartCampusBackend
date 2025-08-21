package com.smartcampus.domain.repositories

import com.smartcampus.domain.models.TeacherWorkloadCreateRequest
import com.smartcampus.domain.models.TeacherWorkloadDto
import com.smartcampus.domain.models.TeacherWorkloadUpdateRequest
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult

interface WorkloadRepository {
    suspend fun getWorkloads(params: PageRequestParams): PaginatedResult<TeacherWorkloadDto>
    suspend fun getWorkloadById(id: Int): TeacherWorkloadDto?
    suspend fun createWorkload(request: TeacherWorkloadCreateRequest): TeacherWorkloadDto
    suspend fun updateWorkload(id: Int, request: TeacherWorkloadUpdateRequest): TeacherWorkloadDto?
    suspend fun deleteWorkload(id: Int): Boolean

    // domain specific helpers
    suspend fun getWorkloadsByTeacher(teacherId: Int, params: PageRequestParams): PaginatedResult<TeacherWorkloadDto>
    suspend fun getWorkloadsByGroup(groupId: Int, params: PageRequestParams): PaginatedResult<TeacherWorkloadDto>
}
