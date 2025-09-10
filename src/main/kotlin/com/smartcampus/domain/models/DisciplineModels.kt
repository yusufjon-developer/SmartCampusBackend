package com.smartcampus.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class DisciplineDto(
    val id: Int,
    val subject: SubjectDto? = null,
    val semester: Int? = null,
    val specialityId: Int? = null,
    val speciality: SpecialityDto? = null,
    val course: Int? = null,
    val lecture: Int? = null,
    val practice: Int? = null,
    val lab: Int? = null,
    val seminar: Int? = null,
    val control: String? = null
)

@Serializable
data class DisciplineCreateRequest(
    val subjectId: Int? = null,
    val semester: Int? = null,
    val specialityId: Int? = null,
    val course: Int? = null,
    val lecture: Int? = null,
    val practice: Int? = null,
    val lab: Int? = null,
    val seminar: Int? = null,
    val control: String? = null
)

@Serializable
data class DisciplineUpdateRequest(
    val subjectId: Int? = null,
    val semester: Int? = null,
    val specialityId: Int? = null,
    val course: Int? = null,
    val lecture: Int? = null,
    val practice: Int? = null,
    val lab: Int? = null,
    val seminar: Int? = null,
    val control: String? = null
)

@Serializable
data class SpecialityDto(val id: Int, val name: String? = null)
