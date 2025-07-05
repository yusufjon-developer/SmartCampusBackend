package com.smartcampus.features.auth

import com.smartcampus.core.models.GenericResponse
import com.smartcampus.features.auth.models.LoginRequest
import com.smartcampus.features.auth.models.NewUser
import com.smartcampus.features.auth.models.TokenResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.log
import io.ktor.server.request.ContentTransformationException
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.post
import io.ktor.server.routing.route

/**
 * Определяет маршруты (эндпоинты) для функциональности аутентификации.
 * Предоставляет эндпоинты для регистрации и входа пользователей.
 *
 * @receiver [Routing] Контекст маршрутизации Ktor, в который добавляются эти маршруты.
 * @param authService Экземпляр [AuthService] для выполнения операций аутентификации.
 * @param application Экземпляр [Application] для доступа к логгеру приложения.
 */
fun Routing.authRoutes(authService: AuthService, application: Application) {

    /**
     * @apidefine auth
     * @apiGroup Аутентификация
     * Группа эндпоинтов для регистрации и входа пользователей.
     */
    route("/auth") {

        /**
         * @api {post} /auth/register Регистрация нового пользователя
         * @apiVersion 0.1.0
         * @apiName RegisterUser
         * @apiGroup Аутентификация
         * @apiDescription Регистрирует нового пользователя в системе.
         * Принимает данные нового пользователя и, в случае успеха, возвращает сообщение об успешной регистрации.
         *
         * @apiBody {String} username Уникальное имя пользователя (логин).
         * @apiBody {String} email Уникальный адрес электронной почты.
         * @apiBody {String} password Пароль пользователя (мин. длина может быть установлена сервером).
         * @apiBody {String} fullName Полное имя пользователя.
         * @apiBody {String} role Роль пользователя (например, "Student", "Teacher").
         *
         * @apiSuccess (201 Created) {Boolean} success Флаг успешности операции (всегда `true`).
         * @apiSuccess (201 Created) {String} message Сообщение об успешной регистрации.
         * @apiSuccessExample {json} Успешный ответ:
         *     HTTP/1.1 201 Created
         *     {
         *       "success": true,
         *       "message": "User 'newuser' registered successfully."
         *     }
         *
         * @apiError (400 Bad Request) {Boolean} success Флаг успешности операции (всегда `false`).
         * @apiError (400 Bad Request) {String} message Описание ошибки (например, "Invalid request body.",
         *           "Username 'existinguser' is already taken.", "Email 'test@example.com' is already registered.").
         * @apiErrorExample {json} Ошибка - Неверное тело запроса:
         *     HTTP/1.1 400 Bad Request
         *     {
         *       "success": false,
         *       "message": "Invalid request body."
         *     }
         * @apiErrorExample {json} Ошибка - Имя пользователя занято:
         *     HTTP/1.1 400 Bad Request
         *     {
         *       "success": false,
         *       "message": "Username 'existinguser' is already taken."
         *     }
         *
         * @apiError (500 Internal Server Error) {Boolean} success Флаг успешности операции (всегда `false`).
         * @apiError (500 Internal Server Error) {String} message Общее сообщение о непредвиденной ошибке сервера.
         * @apiErrorExample {json} Ошибка - Внутренняя ошибка сервера:
         *     HTTP/1.1 500 Internal Server Error
         *     {
         *       "success": false,
         *       "message": "An unexpected error occurred."
         *     }
         */
        post("/register") {
            try {
                val newUserRequest = call.receive<NewUser>()

                authService.registerUser(newUserRequest)
                    .onSuccess { registrationResponse ->
                        call.respond(
                            HttpStatusCode.Created,
                            GenericResponse(true, registrationResponse.message)
                        )
                    }
                    .onFailure { exception ->
                        application.log.warn("Registration failed: ${exception.message}")
                        call.respond(
                            HttpStatusCode.BadRequest,
                            GenericResponse(false, exception.message ?: "Registration failed")
                        )
                    }
            } catch (e: ContentTransformationException) {
                application.log.warn("Registration: Invalid request body for NewUser: ${e.localizedMessage}")
                call.respond(HttpStatusCode.BadRequest, GenericResponse(false, "Invalid request body."))
            } catch (e: Exception) {
                application.log.error("Registration failed unexpectedly", e)
                call.respond(HttpStatusCode.InternalServerError, GenericResponse(false, "An unexpected error occurred."))
            }
        }

        /**
         * @api {post} /auth/login Вход пользователя в систему
         * @apiVersion 0.1.0
         * @apiName LoginUser
         * @apiGroup Аутентификация
         * @apiDescription Аутентифицирует пользователя по имени пользователя и паролю.
         * В случае успеха возвращает JWT. Для пользователей, не являющихся "Student",
         * также требуется передача `deviceUuid` и `deviceType`.
         *
         * @apiBody {String} username Имя пользователя (логин).
         * @apiBody {String} password Пароль пользователя.
         * @apiBody {String} [deviceUuid] Опциональный уникальный идентификатор устройства.
         *                               Обязателен для пользователей с ролью, отличной от "Student".
         * @apiBody {String} [deviceType] Опциональный тип устройства (например, "PC", "Mobile").
         *                                Обязателен для пользователей с ролью, отличной от "Student".
         *
         * @apiSuccess (200 OK) {String} token Сгенерированный JWT.
         * @apiSuccess (200 OK) {String} tokenType Тип токена (всегда "Bearer").
         * @apiSuccessExample {json} Успешный ответ:
         *     HTTP/1.1 200 OK
         *     {
         *       "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
         *       "tokenType": "Bearer"
         *     }
         *
         * @apiError (400 Bad Request) {Boolean} success Флаг успешности операции (всегда `false`).
         * @apiError (400 Bad Request) {String} message Описание ошибки (например, "Invalid request body for LoginRequest.",
         *           "Device UUID and Type are required for non-student users.").
         * @apiError (401 Unauthorized) {Boolean} success Флаг успешности операции (всегда `false`).
         * @apiError (401 Unauthorized) {String} message Описание ошибки (например, "Invalid username or password.",
         *           "User account is inactive.").
         * @apiError (500 Internal Server Error) {Boolean} success Флаг успешности операции (всегда `false`).
         * @apiError (500 Internal Server Error) {String} message Общее сообщение о непредвиденной ошибке сервера.
         * @apiErrorExample {json} Ошибка - Неверные учетные данные:
         *     HTTP/1.1 401 Unauthorized
         *     {
         *       "success": false,
         *       "message": "Invalid username or password."
         *     }
         */
        post("/login") {
            try {
                val loginRequest = call.receive<LoginRequest>()

                authService.loginUser(loginRequest)
                    .onSuccess { authResponse ->
                        call.respond(HttpStatusCode.OK, TokenResponse(authResponse.token))
                    }
                    .onFailure { exception ->
                        application.log.warn("Login failed: ${exception.message}")
                        val statusCode = if (exception is IllegalArgumentException &&
                            (exception.message?.contains("Invalid username or password") == true ||
                                    exception.message?.contains("User account is inactive") == true)) {
                            HttpStatusCode.Unauthorized
                        } else if (exception is IllegalArgumentException &&
                            exception.message?.contains("Device UUID and Type are required") == true) {
                            HttpStatusCode.BadRequest // Явное указание на ошибку запроса
                        }
                        else {
                            HttpStatusCode.BadRequest // Общий BadRequest для других IllegalArgumentException
                        }
                        call.respond(
                            statusCode,
                            GenericResponse(false, exception.message ?: "Login failed")
                        )
                    }
            } catch (e: ContentTransformationException) {
                application.log.warn("Login: Invalid request body: ${e.localizedMessage}")
                call.respond(HttpStatusCode.BadRequest, GenericResponse(false, "Invalid request body for LoginRequest."))
            } catch (e: Exception) {
                application.log.error("Login failed unexpectedly", e)
                call.respond(HttpStatusCode.InternalServerError, GenericResponse(false, "An unexpected error occurred."))
            }
        }
    }
}