package com.smartcampus.features.schedule

import com.smartcampus.data.dao.ScheduleDao
import com.smartcampus.domain.models.*
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.LocalTime

class ScheduleService(private val dao: ScheduleDao, private val workloadDao: com.smartcampus.data.dao.WorkloadDao?) {
    private val log = LoggerFactory.getLogger(ScheduleService::class.java)

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
        val start = try {
            LocalTime.parse(req.startTime)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid startTime format, expected HH:mm")
        }
        val end = try {
            LocalTime.parse(req.endTime)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid endTime format, expected HH:mm")
        }
        if (!end.isAfter(start)) throw IllegalArgumentException("endTime must be after startTime")

        val day = try {
            LocalDate.parse(req.day)
        } catch (e: Exception) {
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
        val start = try {
            LocalTime.parse(req.startTime)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid startTime format, expected HH:mm")
        }
        val end = try {
            LocalTime.parse(req.endTime)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid endTime format, expected HH:mm")
        }
        if (!end.isAfter(start)) throw IllegalArgumentException("endTime must be after startTime")

        val day = try {
            LocalDate.parse(req.day)
        } catch (e: Exception) {
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

    /**
     * Получить структурированное расписание на неделю для преподавателя.
     * Включает все стандартные слоты с информацией о доступности.
     */
    suspend fun getWeeklySchedule(startDay: LocalDate, endDay: LocalDate, teacherId: Int): WeeklyScheduleResponse {
        // 1. Определим стандартные временные слоты пар
        val scheduleSlots = listOf(
            ScheduleSlot(LocalTime.of(8, 0), LocalTime.of(9, 30)),
            ScheduleSlot(LocalTime.of(9, 45), LocalTime.of(11, 15)),
            ScheduleSlot(LocalTime.of(11, 30), LocalTime.of(13, 0)),
            ScheduleSlot(LocalTime.of(13, 0), LocalTime.of(14, 30)),
            ScheduleSlot(LocalTime.of(14, 45), LocalTime.of(16, 15))
        )

        // 2. Получим все расписания преподавателя на этот период с деталями
        val occupiedSchedules = dao.listSchedulesWithDetails(
            teacherId = teacherId,
            day = startDay, // Будет использоваться как начальная дата
        ).filter { schedule ->
            try {
                val scheduleDate = LocalDate.parse(schedule.day)
                scheduleDate >= startDay && scheduleDate <= endDay
            } catch (e: Exception) {
                false
            }
        }

        // 3. Создадим карту: Дата -> Занятые слоты (LocalTime начала -> ScheduleDto)
        val occupiedSlotsByDay = mutableMapOf<LocalDate, MutableList<ScheduleSlotWithDetails>>()
        occupiedSchedules.forEach { schedule ->
            try {
                val scheduleDate = LocalDate.parse(schedule.day)
                val scheduleStart = LocalTime.parse(schedule.startTime)
                val scheduleEnd = LocalTime.parse(schedule.endTime)
                // Создаем объект, связывающий временной слот с деталями занятия
                val slotWithDetails = ScheduleSlotWithDetails(scheduleStart, scheduleEnd, schedule)
                occupiedSlotsByDay.getOrPut(scheduleDate) { mutableListOf() }.add(slotWithDetails)
            } catch (e: Exception) {
                log.warn("Failed to parse schedule date/time for schedule ID ${schedule.id}: ${e.message}")
            }
        }

        // 4. Строим ответ
        val days = mutableListOf<DaySchedule>()
        var currentDate = startDay
        while (!currentDate.isAfter(endDay)) {
            val dayOfWeek = currentDate.dayOfWeek

            // Получаем занятые слоты для текущей даты
            val occupiedForThisDay = occupiedSlotsByDay[currentDate] ?: emptyList()

            // Строим список слотов для этого дня
            val slotsForThisDay = mutableListOf<SlotInfo>()

            for (slot in scheduleSlots) {
                // Ищем, совпадает ли стандартный слот с каким-либо занятием
                val matchingOccupiedSlot = occupiedForThisDay.find { occupiedSlot ->
                    occupiedSlot.start == slot.start && occupiedSlot.end == slot.end
                }

                if (matchingOccupiedSlot != null) {
                    // Слот занят - добавляем информацию о занятии
                    val scheduleDto = matchingOccupiedSlot.schedule
                    slotsForThisDay.add(
                        SlotInfo(
                            startTime = slot.start.toString(),
                            endTime = slot.end.toString(),
                            groupId = scheduleDto.group?.id,
                            groupName = scheduleDto.group?.name,
                            disciplineId = scheduleDto.discipline?.id,
                            disciplineName = scheduleDto.discipline?.subject?.name,
                            isAvailable = false // Слот занят
                        )
                    )
                } else {
                    // Слот свободен
                    slotsForThisDay.add(
                        SlotInfo(
                            startTime = slot.start.toString(),
                            endTime = slot.end.toString(),
                            groupId = null,
                            groupName = null,
                            disciplineId = null,
                            disciplineName = null,
                            isAvailable = true // Слот доступен
                        )
                    )
                }
            }

            days.add(
                DaySchedule(
                    date = currentDate.toString(),
                    dayOfWeek = dayOfWeek.name,
                    slots = slotsForThisDay
                )
            )

            currentDate = currentDate.plusDays(1)
        }

        return WeeklyScheduleResponse(days = days)
    }
}
