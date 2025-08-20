package com.smartcampus.domain.repositories

import com.smartcampus.domain.models.WorkloadExecutionCreateRequest
import com.smartcampus.domain.models.WorkloadExecutionDto
import com.smartcampus.domain.models.WorkloadExecutionUpdateRequest

interface WorkloadExecutionRepository {
    suspend fun listExecutions(workloadId: Int?): List<WorkloadExecutionDto>
    suspend fun getExecutionById(id: Int): WorkloadExecutionDto?
    suspend fun createExecution(request: WorkloadExecutionCreateRequest): WorkloadExecutionDto
    suspend fun updateExecution(id: Int, request: WorkloadExecutionUpdateRequest): WorkloadExecutionDto?
    suspend fun deleteExecution(id: Int): Boolean
}
