package com.smartcampus.domain.repositories

import com.smartcampus.domain.models.employee.EmployeeSignInRequest
import com.smartcampus.domain.models.employee.EmployeeSignInResponse
import com.smartcampus.domain.models.employee.EmployeeSignUpRequest
import com.smartcampus.domain.models.employee.EmployeeSignUpResponse
import com.smartcampus.domain.models.student.StudentSignInRequest
import com.smartcampus.domain.models.student.StudentSignInResponse
import com.smartcampus.domain.models.student.StudentSignUpRequest
import com.smartcampus.domain.models.student.StudentSignUpResponse

interface AuthRepository {
    suspend fun signInStudent(request: StudentSignInRequest): StudentSignInResponse
    suspend fun signInEmployee(request: EmployeeSignInRequest): EmployeeSignInResponse
    suspend fun signUpStudent(request: StudentSignUpRequest): StudentSignUpResponse
    suspend fun signUpEmployee(request: EmployeeSignUpRequest): EmployeeSignUpResponse
}