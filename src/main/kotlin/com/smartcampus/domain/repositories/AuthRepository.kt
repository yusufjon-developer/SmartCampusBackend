package com.smartcampus.domain.repositories

import com.smartcampus.domain.models.auth.RegisterRequest
import com.smartcampus.domain.models.auth.RegisterResponse
import com.smartcampus.domain.models.employee.EmployeeSignInRequest
import com.smartcampus.domain.models.employee.EmployeeSignInResponse
import com.smartcampus.domain.models.student.StudentSignInRequest
import com.smartcampus.domain.models.student.StudentSignInResponse
import com.smartcampus.domain.utils.Either

interface AuthRepository {
    suspend fun registerUser(request: RegisterRequest, passwordHash: String): Either<String, RegisterResponse>
    suspend fun signInStudent(request: StudentSignInRequest): StudentSignInResponse
    suspend fun signInEmployee(request: EmployeeSignInRequest): EmployeeSignInResponse
}