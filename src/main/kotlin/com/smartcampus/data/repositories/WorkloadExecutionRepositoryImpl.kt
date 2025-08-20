package com.smartcampus.data.repositories

import com.smartcampus.data.dao.WorkloadExecutionDao
import com.smartcampus.domain.models.*
import com.smartcampus.domain.repositories.WorkloadExecutionRepository

class WorkloadExecutionRepositoryImpl(private val dao: WorkloadExecutionDao) :
    WorkloadExecutionRepository {
    override suspend fun listExecutions(workloadId: Int?): List<WorkloadExecutionDto> =
        dao.listExecutions(workloadId)

    override suspend fun getExecutionById(id: Int): WorkloadExecutionDto? = dao.getExecutionById(id)
    override suspend fun createExecution(request: WorkloadExecutionCreateRequest): WorkloadExecutionDto =
        dao.createExecution(request)

    override suspend fun updateExecution(
        id: Int,
        request: WorkloadExecutionUpdateRequest
    ): WorkloadExecutionDto? = dao.updateExecution(id, request)

    override suspend fun deleteExecution(id: Int): Boolean = dao.deleteExecution(id)
}
