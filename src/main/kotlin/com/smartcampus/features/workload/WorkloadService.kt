package com.smartcampus.features.workload

import com.smartcampus.data.dao.WorkloadDao
import com.smartcampus.domain.models.*
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult

class WorkloadService(private val dao: WorkloadDao) {

    // allowed types set (you can expand)
    private val allowedTypes = setOf("lecture", "practice", "lab", "seminar", "other")

    suspend fun listWorkloads(params: PageRequestParams): PaginatedResult<TeacherWorkloadDto> {
        return dao.listWorkloads(params)
    }

    suspend fun getWorkload(id: Int): TeacherWorkloadDto? = dao.getWorkloadById(id)

    suspend fun createWorkload(req: TeacherWorkloadCreateRequest): EitherErrorOrDto<TeacherWorkloadDto> {
        // basic validations
        if (req.hours != null && req.hours < 0) return EitherErrorOrDto.Error("hours must be >= 0")
        if (req.type != null && !allowedTypes.contains(req.type.lowercase())) return EitherErrorOrDto.Error("invalid type, allowed: $allowedTypes")
        // You can add checks: teacher exists, discipline exists, group exists (optional)
        val created = dao.createWorkload(req)
        return EitherErrorOrDto.Ok(created)
    }

    suspend fun updateWorkload(id: Int, req: TeacherWorkloadUpdateRequest): TeacherWorkloadDto? {
        // validate
        req.hours?.let { if (it < 0) throw IllegalArgumentException("hours must be >= 0") }
        req.type?.let { if (!allowedTypes.contains(it.lowercase())) throw IllegalArgumentException("invalid type") }
        return dao.updateWorkload(id, req)
    }

    suspend fun deleteWorkload(id: Int): Boolean = dao.deleteWorkload(id)

    suspend fun listForTeacher(teacherId: Int, academicYear: String?): List<TeacherWorkloadDto> =
        dao.getWorkloadsForTeacher(teacherId, academicYear)

    // Executions
    suspend fun listExecutions(workloadId: Int): List<WorkloadExecutionDto> = dao.listExecutionsForWorkload(workloadId)

    suspend fun addExecution(workloadId: Int, req: WorkloadExecutionCreateRequest): WorkloadExecutionDto? {
        if (req.hours <= 0.0) throw IllegalArgumentException("hours must be > 0")
        // optionally check sum vs planned (not enforced)
        return dao.createExecution(workloadId, req)
    }

    suspend fun sumExecutedHours(workloadId: Int): Double = dao.sumExecutedHours(workloadId)

    // small Either wrapper to return errors without exceptions in create
    sealed interface EitherErrorOrDto<out T> {
        data class Ok<T>(val value: T) : EitherErrorOrDto<T>
        data class Error(val message: String) : EitherErrorOrDto<Nothing>
    }
}
