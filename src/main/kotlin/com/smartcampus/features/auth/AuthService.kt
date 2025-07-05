package com.smartcampus.features.auth

import com.smartcampus.core.database.auth.entities.UserDevicesTable
import com.smartcampus.core.database.auth.entities.UsersTable
import com.smartcampus.core.security.PasswordHasher
import com.smartcampus.core.security.TokenUtils
import com.smartcampus.features.auth.models.AuthResponse
import com.smartcampus.features.auth.models.LoginRequest
import com.smartcampus.features.auth.models.NewUser
import com.smartcampus.features.auth.models.RegistrationResponse

/**
 * Сервис, отвечающий за бизнес-логику аутентификации и управления пользователями.
 * Инкапсулирует процессы регистрации новых пользователей и входа существующих пользователей в систему.
 * Взаимодействует с [AuthRepository] для доступа к данным, [PasswordHasher] для работы с паролями
 * и [TokenUtils] для генерации JWT.
 *
 * @property authRepository Репозиторий для доступа к данным аутентификации.
 * @property passwordHasher Утилита для хеширования и проверки паролей.
 * @property tokenUtils Утилита для генерации JWT.
 * @constructor Создает экземпляр AuthService.
 */
class AuthService(
    private val authRepository: AuthRepository,
    private val passwordHasher: PasswordHasher,
    private val tokenUtils: TokenUtils
) {

    /**
     * Регистрирует нового пользователя в системе.
     *
     * Выполняет следующие проверки:
     * - Уникальность имени пользователя.
     * - Уникальность адреса электронной почты.
     * Хеширует пароль перед сохранением пользователя в базе данных.
     *
     * @param newUser Объект [NewUser], содержащий данные для регистрации нового пользователя.
     * @return [Result] который содержит:
     *         - [RegistrationResponse] в случае успешной регистрации.
     *         - [IllegalArgumentException] если имя пользователя или email уже заняты.
     *         - [Exception] при других ошибках сервера во время создания пользователя.
     */
    suspend fun registerUser(newUser: NewUser): Result<RegistrationResponse> {
        if (authRepository.findUserByUsername(newUser.username) != null) {
            return Result.failure(IllegalArgumentException("Username '${newUser.username}' is already taken."))
        }

        if (authRepository.findUserByEmail(newUser.email) != null) {
            return Result.failure(IllegalArgumentException("Email '${newUser.email}' is already registered."))
        }

        val hashedPassword = passwordHasher.hashPassword(newUser.password)

        return try {
            val userId = authRepository.createUser(
                username = newUser.username,
                email = newUser.email,
                passwordHash = hashedPassword,
                fullName = newUser.fullName,
                role = newUser.role,
                isActive = true
            )
            Result.success(
                RegistrationResponse(
                    userId,
                    newUser.username,
                    "User registered successfully."
                )
            )
        } catch (e: Exception) {
            println("Error creating user: ${e.message}")
            Result.failure(Exception("Failed to register user due to a server error."))
        }
    }

    /**
     * Аутентифицирует пользователя и, в случае успеха, генерирует для него JWT.
     *
     * Выполняет следующие проверки:
     * - Существование пользователя с указанным именем.
     * - Активность учетной записи пользователя.
     * - Соответствие предоставленного пароля хешированному паролю в базе данных.
     * - Для пользователей, не являющихся "Student", проверяет наличие `deviceUuid` и `deviceType` в запросе.
     *
     * Для пользователей, не являющихся "Student", также выполняется логика управления устройствами:
     * - Если устройство уже зарегистрировано, обновляется время последнего входа.
     * - Если это новое устройство, а у пользователя уже есть одобренное устройство того же типа,
     *   одобрение со старого устройства отзывается.
     * - Новое устройство регистрируется (изначально как неодобренное).
     *
     * @param loginRequest Объект [LoginRequest], содержащий учетные данные пользователя и информацию об устройстве.
     * @return [Result] который содержит:
     *         - [AuthResponse] с JWT и информацией о пользователе в случае успешного входа.
     *         - [IllegalArgumentException] при неверных учетных данных, неактивной учетной записи,
     *           отсутствии необходимой информации об устройстве или других ошибках валидации.
     */
    suspend fun loginUser(loginRequest: LoginRequest): Result<AuthResponse> {
        val userRow = authRepository.findUserByUsername(loginRequest.username)
            ?: return Result.failure(IllegalArgumentException("Invalid username or password."))

        if (!userRow[UsersTable.isActive]) {
            return Result.failure(IllegalArgumentException("User account is inactive."))
        }

        if (!passwordHasher.checkPassword(loginRequest.password, userRow[UsersTable.passwordHash])) {
            return Result.failure(IllegalArgumentException("Invalid username or password."))
        }

        val userId = userRow[UsersTable.id].value
        val username = userRow[UsersTable.username]
        val role = userRow[UsersTable.role]

        var tokenPair: Pair<String, java.util.Date>

        if (role == "Student") {
            tokenPair = tokenUtils.generateToken(userId, username, role)
        } else {
            val deviceUuid = loginRequest.deviceUuid
            val deviceType = loginRequest.deviceType

            if (deviceUuid == null || deviceType == null) {
                return Result.failure(IllegalArgumentException("Device UUID and Type are required for non-student users."))
            }

            val existingDevice = authRepository.findUserDevice(userId, deviceUuid)

            if (existingDevice != null) {
                authRepository.updateUserDeviceLastLogin(existingDevice[UserDevicesTable.id].value)
            } else {
                val oldApprovedDevice = authRepository.findApprovedDeviceByType(userId, deviceType)
                if (oldApprovedDevice != null) {
                    authRepository.revokeDeviceApproval(oldApprovedDevice[UserDevicesTable.id].value)
                }
                authRepository.registerNewDevice(userId, deviceUuid, deviceType)
            }
            tokenPair = tokenUtils.generateToken(userId, username, role, deviceUuid, deviceType)
        }

        return Result.success(
            AuthResponse(
                token = tokenPair.first,
                userId = userId,
                username = username,
                role = role
            )
        )
    }
}
