package com.smartcampus.features.schedule

import com.smartcampus.data.dao.ScheduleDao
import com.smartcampus.domain.models.ScheduleCreateRequest
import com.smartcampus.domain.models.ScheduleDto
import com.smartcampus.domain.models.ScheduleUpdateRequest
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.LocalTime

class ScheduleService(private val dao: ScheduleDao, private val workloadDao: com.smartcampus.data.dao.WorkloadDao?) {
    private val log = LoggerFactory.getLogger(ScheduleService::class.java)

    // list (filters)
    suspend fun listSchedules(
        day: String? = null,
        teacherId: Int? = null,
        groupId: Int? = null,
        auditoriumId: Int? = null
    ): List<ScheduleDto> {
        val d = day?.let { LocalDate.parse(it) }
        return dao.listSchedules(d, teacherId, groupId, auditoriumId)
    }

    suspend fun getSchedule(id: Int): ScheduleDto? = dao.getById(id)

    suspend fun createSchedule(req: ScheduleCreateRequest): ScheduleDto {
        val start = try { LocalTime.parse(req.startTime) } catch (e: Exception) {
            throw IllegalArgumentException("Invalid startTime format, expected HH:mm")
        }
        val end = try { LocalTime.parse(req.endTime) } catch (e: Exception) {
            throw IllegalArgumentException("Invalid endTime format, expected HH:mm")
        }
        if (!end.isAfter(start)) throw IllegalArgumentException("endTime must be after startTime")

        val day = try { LocalDate.parse(req.day) } catch (e: Exception) {
            throw IllegalArgumentException("Invalid day format, expected yyyy-MM-dd")
        }

        // if workloadId provided, try to derive teacherId/groupId
        var teacherId = req.teacherId
        var groupId = req.groupId
        if (req.workloadId != null && workloadDao != null) {
            val wl = workloadDao.getWorkloadById(req.workloadId)
                ?: throw IllegalArgumentException("Workload with id ${req.workloadId} not found")
            // WorkloadDao.getWorkloadById returns TeacherWorkloadDto with teacherId/groupId fields
            teacherId = teacherId ?: wl.teacherId
            groupId = groupId ?: wl.groupId
        }

        // find conflicts
        val conflicts = dao.findConflicts(day, start, end, teacherId, groupId, req.auditoriumId, excludeId = null)
        if (conflicts.isNotEmpty()) {
            // provide details
            val msg = "Schedule conflict detected: ${conflicts.size} overlapping entries"
            log.warn(msg + " -> ${conflicts.map { it.id }}")
            throw IllegalStateException(msg)
        }

        val created = dao.create(req) ?: throw IllegalStateException("Failed to create schedule")
        return created
    }

    suspend fun updateSchedule(id: Int, req: ScheduleUpdateRequest): ScheduleDto {
        val start = try { LocalTime.parse(req.startTime) } catch (e: Exception) {
            throw IllegalArgumentException("Invalid startTime format, expected HH:mm")
        }
        val end = try { LocalTime.parse(req.endTime) } catch (e: Exception) {
            throw IllegalArgumentException("Invalid endTime format, expected HH:mm")
        }
        if (!end.isAfter(start)) throw IllegalArgumentException("endTime must be after startTime")

        val day = try { LocalDate.parse(req.day) } catch (e: Exception) {
            throw IllegalArgumentException("Invalid day format, expected yyyy-MM-dd")
        }

        val teacherId = req.teacherId
        val groupId = req.groupId

        val conflicts = dao.findConflicts(day, start, end, teacherId, groupId, req.auditoriumId, excludeId = id)
        if (conflicts.isNotEmpty()) {
            val msg = "Schedule conflict detected: ${conflicts.size} overlapping entries"
            log.warn(msg + " -> ${conflicts.map { it.id }}")
            throw IllegalStateException(msg)
        }

        return dao.update(id, req) ?: throw IllegalArgumentException("Schedule with id $id not found")
    }

    suspend fun deleteSchedule(id: Int): Boolean = dao.delete(id)

    /**
     * Validate request (returns empty list if OK, otherwise list of conflicting ScheduleDto)
     */
    suspend fun validateRequest(req: ScheduleCreateRequest): List<ScheduleDto> {
        val start = LocalTime.parse(req.startTime)
        val end = LocalTime.parse(req.endTime)
        val day = LocalDate.parse(req.day)
        var teacherId = req.teacherId
        var groupId = req.groupId
        if (req.workloadId != null && workloadDao != null) {
            val wl = workloadDao.getWorkloadById(req.workloadId)
            teacherId = teacherId ?: wl?.teacherId
            groupId = groupId ?: wl?.groupId
        }
        return dao.findConflicts(day, start, end, teacherId, groupId, req.auditoriumId, excludeId = null)
    }
}
