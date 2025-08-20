// file: com/smartcampus/domain/repositories/AttendanceRepository.kt
package com.smartcampus.domain.repositories

import com.smartcampus.domain.models.AttendanceCreateRequest
import com.smartcampus.domain.models.AttendanceRecordDto
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult

interface AttendanceRepository {
    suspend fun markAttendance(request: AttendanceCreateRequest): AttendanceRecordDto
    suspend fun getAttendanceById(id: Int): AttendanceRecordDto?
    suspend fun updateAttendance(id: Int, request: AttendanceCreateRequest): AttendanceRecordDto?
    suspend fun deleteAttendance(id: Int): Boolean
    suspend fun getAttendanceForStudent(studentId: Int, params: PageRequestParams): PaginatedResult<AttendanceRecordDto>
    suspend fun getAttendanceForDiscipline(disciplineId: Int, params: PageRequestParams): PaginatedResult<AttendanceRecordDto>
}
