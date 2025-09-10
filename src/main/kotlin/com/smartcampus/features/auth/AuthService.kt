package com.smartcampus.features.auth

import com.smartcampus.domain.models.EmployeeSignInRequest
import com.smartcampus.domain.models.EmployeeSignInResponse
import com.smartcampus.domain.models.StudentSignInRequest
import com.smartcampus.domain.models.StudentSignInResponse
import com.smartcampus.domain.models.auth.RegisterRequest
import com.smartcampus.domain.models.auth.RegisterResponse
import com.smartcampus.domain.repositories.AuthRepository
import com.smartcampus.domain.security.PasswordHasher
import com.smartcampus.domain.utils.Either

class AuthService(
    private val repository: AuthRepository,
    private val passwordHasher: PasswordHasher
) {

    suspend fun signInStudent(request: StudentSignInRequest): StudentSignInResponse {
        return repository.signInStudent(request)
    }

    suspend fun signInEmployee(request: EmployeeSignInRequest): EmployeeSignInResponse {
        return repository.signInEmployee(request)
    }


    suspend fun register(request: RegisterRequest): Either<String, RegisterResponse> {
        if (request.username.isBlank() || request.password.isBlank()) {
            return Either.Left("Username and password are required")
        }
        val hashed = passwordHasher.hashPassword(request.password)
        return repository.registerUser(request, hashed)
    }
}