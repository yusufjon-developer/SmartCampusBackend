package com.smartcampus.domain.models.auth

import com.smartcampus.domain.models.StudentDetailsDto
import com.smartcampus.domain.models.StudentSensitiveDto
import com.smartcampus.domain.models.TeacherDetailsDto
import com.smartcampus.domain.models.TeacherSensitiveDto
import kotlinx.serialization.Serializable

/** Ответ после регистрации */
@Serializable
data class RegisterResponse(
    val userId: Int,
    val username: String,
    val role: String?,
    val studentProfile: StudentDetailsDto?,
    val studentSensitive: StudentSensitiveDto?,
    val teacherProfile: TeacherDetailsDto?,
    val teacherSensitive: TeacherSensitiveDto?,
)
