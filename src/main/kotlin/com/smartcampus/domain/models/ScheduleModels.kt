package com.smartcampus.domain.models

import kotlinx.serialization.Serializable
import java.time.LocalTime

@Serializable
data class ScheduleDto(
    val id: Int,
    val workloadId: Int? = null,
    val day: String,         // "yyyy-MM-dd"
    val startTime: String,   // "HH:mm"
    val endTime: String,     // "HH:mm"
    val teacher: TeacherDetailsDto? = null,
    val group: GroupDto? = null,
    val discipline: DisciplineDto? = null,
    val auditorium: AuditoriumDetailsDto? = null,
    val type: String? = null
)

@Serializable
data class ScheduleCreateRequest(
    val workloadId: Int? = null,
    val day: String,
    val startTime: String,
    val endTime: String,
    val teacherId: Int? = null,
    val groupId: Int? = null,
    val disciplineId: Int? = null,
    val auditoriumId: Int? = null,
    val type: String? = null
)

@Serializable
data class ScheduleUpdateRequest(
    val workloadId: Int? = null,
    val day: String,
    val startTime: String,
    val endTime: String,
    val teacherId: Int? = null,
    val groupId: Int? = null,
    val disciplineId: Int? = null,
    val auditoriumId: Int? = null,
    val type: String? = null
)

@Serializable
data class WeeklyFreeSlotsRequest(
    val startDay: String,
    val endDay: String,
    val teacherId: Int?
)

@Serializable
data class WeeklyScheduleResponse(
    val days: List<DaySchedule>
)

@Serializable
data class DaySchedule(
    val date: String,
    val dayOfWeek: String,
    val slots: List<SlotInfo>
)

@Serializable
data class SlotInfo(
    val startTime: String,
    val endTime: String,
    val groupId: Int?,
    val groupName: String?,
    val disciplineId: Int?,
    val disciplineName: String?,
    val isAvailable: Boolean
)

data class ScheduleSlot(val start: LocalTime, val end: LocalTime)

data class ScheduleSlotWithDetails(
    val start: LocalTime,
    val end: LocalTime,
    val schedule: ScheduleDto
)


