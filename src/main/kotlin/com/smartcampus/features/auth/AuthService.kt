package com.smartcampus.features.auth

import com.smartcampus.domain.models.employee.EmployeeSignInRequest
import com.smartcampus.domain.models.employee.EmployeeSignInResponse
import com.smartcampus.domain.models.employee.EmployeeSignUpRequest
import com.smartcampus.domain.models.employee.EmployeeSignUpResponse
import com.smartcampus.domain.models.student.StudentSignInRequest
import com.smartcampus.domain.models.student.StudentSignInResponse
import com.smartcampus.domain.models.student.StudentSignUpRequest
import com.smartcampus.domain.models.student.StudentSignUpResponse
import com.smartcampus.domain.repositories.AuthRepository

class AuthService(private val authRepository: AuthRepository) {

    suspend fun signInStudent(request: StudentSignInRequest): StudentSignInResponse {
        return authRepository.signInStudent(request)
    }

    suspend fun signInEmployee(request: EmployeeSignInRequest): EmployeeSignInResponse {
        return authRepository.signInEmployee(request)
    }


    suspend fun signUpStudent(request: StudentSignUpRequest): StudentSignUpResponse {
        return authRepository.signUpStudent(request)
    }

    suspend fun signUpEmployee(request: EmployeeSignUpRequest): EmployeeSignUpResponse {
        return authRepository.signUpEmployee(request)
    }
}