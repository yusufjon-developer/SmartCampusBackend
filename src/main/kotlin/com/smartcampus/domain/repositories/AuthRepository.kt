package com.smartcampus.domain.repositories

import com.smartcampus.domain.models.EmployeeSignInRequest
import com.smartcampus.domain.models.EmployeeSignInResponse
import com.smartcampus.domain.models.StudentSignInRequest
import com.smartcampus.domain.models.StudentSignInResponse
import com.smartcampus.domain.models.auth.RegisterRequest
import com.smartcampus.domain.models.auth.RegisterResponse
import com.smartcampus.domain.utils.Either

interface AuthRepository {
    suspend fun registerUser(request: RegisterRequest, passwordHash: String): Either<String, RegisterResponse>
    suspend fun signInStudent(request: StudentSignInRequest): StudentSignInResponse
    suspend fun signInEmployee(request: EmployeeSignInRequest): EmployeeSignInResponse
}