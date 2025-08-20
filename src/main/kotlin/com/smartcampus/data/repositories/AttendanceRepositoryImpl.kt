package com.smartcampus.data.repositories

import com.smartcampus.data.dao.AttendanceDao
import com.smartcampus.domain.models.AttendanceCreateRequest
import com.smartcampus.domain.models.AttendanceRecordDto
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult
import com.smartcampus.domain.repositories.AttendanceRepository

class AttendanceRepositoryImpl(private val dao: AttendanceDao) : AttendanceRepository {
    override suspend fun markAttendance(request: AttendanceCreateRequest): AttendanceRecordDto = dao.markAttendance(request)
    override suspend fun getAttendanceById(id: Int): AttendanceRecordDto? = dao.getAttendanceById(id)
    override suspend fun updateAttendance(id: Int, request: AttendanceCreateRequest): AttendanceRecordDto? = dao.updateAttendance(id, request)
    override suspend fun deleteAttendance(id: Int): Boolean = dao.deleteAttendance(id)
    override suspend fun getAttendanceForStudent(studentId: Int, params: PageRequestParams): PaginatedResult<AttendanceRecordDto> = dao.getAttendanceForStudent(studentId, params)
    override suspend fun getAttendanceForDiscipline(disciplineId: Int, params: PageRequestParams): PaginatedResult<AttendanceRecordDto> = dao.getAttendanceForDiscipline(disciplineId, params)
}
