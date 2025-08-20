package com.smartcampus.data.repositories

import com.smartcampus.data.dao.WorkloadDao
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult
import com.smartcampus.domain.models.TeacherWorkloadCreateRequest
import com.smartcampus.domain.models.TeacherWorkloadDto
import com.smartcampus.domain.models.TeacherWorkloadUpdateRequest
import com.smartcampus.domain.repositories.WorkloadRepository

class WorkloadRepositoryImpl(private val dao: WorkloadDao) : WorkloadRepository {
    override suspend fun getWorkloads(params: PageRequestParams): PaginatedResult<TeacherWorkloadDto> =
        dao.getWorkloads(params)

    override suspend fun getWorkloadById(id: Int): TeacherWorkloadDto? = dao.getWorkloadById(id)
    override suspend fun createWorkload(request: TeacherWorkloadCreateRequest): TeacherWorkloadDto =
        dao.createWorkload(request)

    override suspend fun updateWorkload(
        id: Int,
        request: TeacherWorkloadUpdateRequest
    ): TeacherWorkloadDto? = dao.updateWorkload(id, request)

    override suspend fun deleteWorkload(id: Int): Boolean = dao.deleteWorkload(id)
    override suspend fun getWorkloadsByTeacher(
        teacherId: Int,
        params: PageRequestParams
    ): PaginatedResult<TeacherWorkloadDto> = dao.getWorkloadsByTeacher(teacherId, params)

    override suspend fun getWorkloadsByGroup(
        groupId: Int,
        params: PageRequestParams
    ): PaginatedResult<TeacherWorkloadDto> = dao.getWorkloadsByGroup(groupId, params)
}
