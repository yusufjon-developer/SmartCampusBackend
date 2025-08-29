package com.smartcampus.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class CurriculumDto(
    val id: Int,
    val specialityId: Int? = null,
    val speciality: SpecialityDto? = null,
    val year: Int? = null,
    val profile: String? = null,
    val educationForm: String? = null,
    val degree: String? = null,
    val duration: Int? = null,
    val approvedDate: String? = null,
    val disciplines: List<CurriculumDisciplineDto> = emptyList()
)

@Serializable
data class CurriculumCreateRequest(
    val specialityId: Int? = null,
    val year: Int? = null,
    val profile: String? = null,
    val educationForm: String? = null,
    val degree: String? = null,
    val duration: Int? = null,
    val approvedDate: String? = null,
    val disciplines: List<CurriculumDisciplineCreateRequest>? = null
)

@Serializable
data class CurriculumUpdateRequest(
    val specialityId: Int? = null,
    val year: Int? = null,
    val profile: String? = null,
    val educationForm: String? = null,
    val degree: String? = null,
    val duration: Int? = null,
    val approvedDate: String? = null,
    val disciplines: List<CurriculumDisciplineCreateRequest>? = null
)

@Serializable
data class CurriculumDisciplineDto(
    val id: Int,
    val curriculumId: Int,
    val disciplineId: Int? = null,
    val discipline: DisciplineDto? = null,
    val semester: Int? = null,
    val course: Int? = null,
    val exam: Boolean = false,
    val credit: Boolean = false,
    val coursework: Boolean = false,
    val controlType: String? = null,
    val credits: Int? = null
)

@Serializable
data class CurriculumDisciplineCreateRequest(
    val disciplineId: Int? = null,
    val semester: Int? = null,
    val course: Int? = null,
    val exam: Boolean? = null,
    val credit: Boolean? = null,
    val coursework: Boolean? = null,
    val controlType: String? = null,
    val credits: Int? = null
)

@Serializable
data class CurriculumDisciplineUpdateRequest(
    val disciplineId: Int? = null,
    val semester: Int? = null,
    val course: Int? = null,
    val exam: Boolean? = null,
    val credit: Boolean? = null,
    val coursework: Boolean? = null,
    val controlType: String? = null,
    val credits: Int? = null
)

@Serializable
data class GroupDto(
    val id: Int,
    val name: String? = null,
    val course: Int? = null,
    val speciality: SpecialityDto? = null
)

// Request DTOs

@Serializable
data class SpecialityCreateRequest(
    val name: String
)

@Serializable
data class SpecialityUpdateRequest(
    val name: String? = null
)

@Serializable
data class GroupCreateRequest(
    val name: String,
    val specId: Int? = null,
    val course: Int? = null
)

@Serializable
data class GroupUpdateRequest(
    val name: String? = null,
    val specId: Int? = null,
    val course: Int? = null
)

