package com.smartcampus.data.repositories

import com.smartcampus.data.database.auth.dao.AuthDao
import com.smartcampus.data.database.auth.entities.RolesTable
import com.smartcampus.data.database.auth.entities.UserDevicesTable
import com.smartcampus.data.database.auth.entities.UsersTable
import com.smartcampus.domain.models.employee.EmployeeSignInRequest
import com.smartcampus.domain.models.employee.EmployeeSignInResponse
import com.smartcampus.domain.models.employee.EmployeeSignUpRequest
import com.smartcampus.domain.models.employee.EmployeeSignUpResponse
import com.smartcampus.domain.models.student.StudentSignInRequest
import com.smartcampus.domain.models.student.StudentSignInResponse
import com.smartcampus.domain.models.student.StudentSignUpRequest
import com.smartcampus.domain.models.student.StudentSignUpResponse
import com.smartcampus.domain.repositories.AuthRepository
import com.smartcampus.domain.security.PasswordHasher
import com.smartcampus.domain.security.TokenUtils
import io.ktor.server.util.toLocalDateTime
import io.ktor.utils.io.InternalAPI
import java.time.LocalDateTime

class AuthRepositoryImpl(
    private val authDao: AuthDao,
    private val tokenUtils: TokenUtils,
    private val passwordHasher: PasswordHasher
) : AuthRepository {

    override suspend fun signInStudent(request: StudentSignInRequest): StudentSignInResponse {
        val userRow = authDao.findUserByEmailWithRole(request.email)
            ?: throw SecurityException("Student not found or incorrect email.")

        val roleName = userRow[RolesTable.name]
        if (!roleName.equals("Student", ignoreCase = true)) {
            throw SecurityException("Access denied. User is not a student.")
        }

        if (!passwordHasher.checkPassword(request.password, userRow[UsersTable.passwordHash])) {
            throw SecurityException("Invalid password.")
        }

        if (!userRow[UsersTable.isActive]) {
            throw SecurityException("User account is inactive.")
        }

        val userId = userRow[UsersTable.id].value

        val (token, _) = tokenUtils.generateToken(
            userId = userId,
            username = userRow[UsersTable.username],
            roleId = userRow[RolesTable.id].value
        )
        return StudentSignInResponse(token)
    }

    override suspend fun signInEmployee(request: EmployeeSignInRequest): EmployeeSignInResponse {
        val userRow = authDao.findUserByEmailWithRole(request.email)
            ?: throw SecurityException("Employee not found or incorrect email.")

        val roleName = userRow[RolesTable.name]
        if (roleName.equals("Student", ignoreCase = true)) {
            throw SecurityException("Access denied. Students cannot sign in as employees.")
        }

        if (!passwordHasher.checkPassword(request.password, userRow[UsersTable.passwordHash])) {
            throw SecurityException("Invalid password.")
        }

        if (!userRow[UsersTable.isActive]) {
            throw SecurityException("User account is inactive.")
        }

        val userId = userRow[UsersTable.id].value
        val userDeviceRow = authDao.findUserDevice(userId, request.uuid)

        val currentTime = LocalDateTime.now()

        if (userDeviceRow == null) {
            authDao.registerNewDevice(
                userId = userId,
                deviceUuid = request.uuid,
                isApproved = false,
                registeredAt = currentTime,
                lastLoginAt = null
            )
            throw SecurityException("Device registered but requires administrator approval.")
        }

        if (!userDeviceRow[UserDevicesTable.isApproved]) {
            throw SecurityException("Device is not approved. Please contact administrator.")
        }

        authDao.updateUserDeviceLastLogin(userDeviceRow[UserDevicesTable.id].value, currentTime)

        val (token, _) = tokenUtils.generateToken(
            userId = userId,
            username = userRow[UsersTable.username],
            roleId = userRow[RolesTable.id].value,
            deviceUuid = request.uuid
        )
        return EmployeeSignInResponse(token)
    }

    @OptIn(InternalAPI::class)
    override suspend fun signUpStudent(request: StudentSignUpRequest): StudentSignUpResponse {
        if (authDao.findUserByEmailWithRole(request.email) != null) {
            throw IllegalArgumentException("User with email '${request.email}' already exists.")
        }
        if (authDao.findUserByUsernameWithRole(request.username) != null) {
            throw IllegalArgumentException("User with username '${request.username}' already exists.")
        }

        val studentRoleRow = authDao.findRoleByName("Student")
            ?: throw IllegalStateException("'Student' role not found in database. Please ensure it exists.")
        val studentRoleId = studentRoleRow[RolesTable.id].value

        val hashedPassword = passwordHasher.hashPassword(request.password)
        val currentTime = LocalDateTime.now()

        val newUserId = authDao.createUser(
            username = request.username,
            email = request.email,
            passwordHash = hashedPassword,
            fullName = request.fullName,
            roleId = studentRoleId,
            isActive = true,
            createdAt = currentTime
        )

        val (token, expiresAt) = tokenUtils.generateToken(
            userId = newUserId,
            username = request.username,
            roleId = studentRoleId
        )

        return StudentSignUpResponse(
            userId = newUserId,
            message = "Student account created successfully.",
            token = token,
            expiresAt = expiresAt.toLocalDateTime()
        )
    }

    override suspend fun signUpEmployee(request: EmployeeSignUpRequest): EmployeeSignUpResponse {
        if (authDao.findUserByEmailWithRole(request.email) != null) {
            throw IllegalArgumentException("User with email '${request.email}' already exists.")
        }
        if (authDao.findUserByUsernameWithRole(request.username) != null) {
            throw IllegalArgumentException("User with username '${request.username}' already exists.")
        }

        if (request.roleName.equals("Student", ignoreCase = true)) {
            throw IllegalArgumentException("Cannot register a 'Student' via employee sign-up.")
        }

        val employeeRoleRow = authDao.findRoleByName(request.roleName)
            ?: throw IllegalStateException("Role '${request.roleName}' not found. Please ensure it exists or use a valid role name.")
        val employeeRoleId = employeeRoleRow[RolesTable.id].value

        val hashedPassword = passwordHasher.hashPassword(request.password)
        val currentTime = LocalDateTime.now()

        val newUserId = authDao.createUser(
            username = request.username,
            email = request.email,
            passwordHash = hashedPassword,
            fullName = request.fullName,
            roleId = employeeRoleId,
            isActive = false,
            createdAt = currentTime
        )

        return EmployeeSignUpResponse(
            userId = newUserId,
            message = "Employee account created successfully. It may require administrator activation."
        )
    }
}