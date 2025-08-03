package com.smartcampus.features.auth

import com.smartcampus.domain.models.employee.EmployeeSignInRequest
import com.smartcampus.domain.models.employee.EmployeeSignUpRequest
import com.smartcampus.domain.models.student.StudentSignInRequest
import com.smartcampus.domain.models.student.StudentSignUpRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.log
import io.ktor.server.request.ContentTransformationException
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.authRoutes(service: AuthService) {

    route("/auth") {
        post("/student/signin") {
            try {
                val request = call.receive<StudentSignInRequest>()
                val response = service.signInStudent(request)
                call.respond(HttpStatusCode.OK, response)
            } catch (e: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request body: ${e.localizedMessage}"))
            } catch (e: SecurityException) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to e.localizedMessage))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.Conflict, mapOf("error" to e.localizedMessage))
            } catch (e: Exception) {
                application.log.error("Failed to sign in student", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "An unexpected error occurred."))
            }
        }

        post("/student/signup") {
            // TODO: SEC-001: Реализовать проверку прав доступа.
            //  Кто может регистрировать нового студента?
            //  - Любой желающий (публичная регистрация)?
            //  - Только аутентифицированный администратор/сотрудник?
            //  Если требуется аутентификация, этот эндпоинт нужно будет защитить
            //  и проверять роль/права вызывающего.
            //  Пример:
            //  val principal = call.principal<JWTPrincipal>()
            //  if (principal == null || !hasPermissionToRegisterStudent(principal)) {
            //      call.respond(HttpStatusCode.Forbidden, mapOf("error" to "You do not have permission to register students."))
            //      return@post
            //  }

            try {
                val request = call.receive<StudentSignUpRequest>()
                if (request.username.isBlank() || request.email.isBlank() || request.password.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Username, email, and password cannot be empty."))
                    return@post
                }

                val response = service.signUpStudent(request)
                call.respond(HttpStatusCode.Created, response)
            } catch (e: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request body: ${e.localizedMessage}"))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.Conflict, mapOf("error" to e.localizedMessage))
            } catch (e: IllegalStateException) {
                application.log.error("Configuration error during student sign up", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Server configuration error: ${e.localizedMessage}"))
            } catch (e: Exception) {
                application.log.error("Failed to sign up student", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "An unexpected error occurred."))
            }
        }

        // Сюда можно будет добавить /auth/student/signup, /auth/student/refresh-token и т.д.
    }

    route("/crm/auth") {
        post("/employee/signin") {
            try {
                val request = call.receive<EmployeeSignInRequest>()
                val response = service.signInEmployee(request)
                call.respond(HttpStatusCode.OK, response)
            } catch (e: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request body: ${e.localizedMessage}"))
            } catch (e: SecurityException) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to e.localizedMessage))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.Conflict, mapOf("error" to e.localizedMessage))
            } catch (e: Exception) {
                application.log.error("Failed to sign in employee", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "An unexpected error occurred."))
            }
        }

        post("/employee/signup") {
            // TODO: SEC-002: Реализовать проверку прав доступа.
            //  Регистрация сотрудников почти наверняка должна быть доступна
            //  только аутентифицированным пользователям с определенными правами
            //  (например, SystemAdmin, DepartmentAdmin, HR-менеджер).
            //  Этот эндпоинт нужно будет защитить с помощью authenticate {} блока
            //  и проверять роль/права вызывающего.
            //  Пример:
            //  authenticate("auth-jwt") { // или ваше имя конфигурации аутентификации
            //      post("/employee/signup") { // будет внутри authenticate блока
            //          val principal = call.principal<JWTPrincipal>() // или ваш тип principal
            //          if (principal == null || !hasPermissionToRegisterEmployee(principal)) {
            //              call.respond(HttpStatusCode.Forbidden, mapOf("error" to "You do not have permission to register employees."))
            //              return@post
            //          }
            //          // ... остальная логика регистрации ...
            //      }
            //  }
            //  Пока что этот TODO находится вне блока authenticate для простоты текущего этапа,
            //  но при реализации его нужно будет переместить внутрь защищенного роута.

            try {
                val request = call.receive<EmployeeSignUpRequest>()
                if (request.username.isBlank() || request.email.isBlank() || request.password.isBlank() || request.roleName.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Username, email, password, and role name cannot be empty."))
                    return@post
                }

                val response = service.signUpEmployee(request)
                call.respond(HttpStatusCode.Created, response)
            } catch (e: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request body: ${e.localizedMessage}"))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.Conflict, mapOf("error" to e.localizedMessage))
            } catch (e: IllegalStateException) {
                application.log.error("Configuration error during employee sign up", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Server configuration error: ${e.localizedMessage}"))
            } catch (e: Exception) {
                application.log.error("Failed to sign up employee", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "An unexpected error occurred."))
            }
        }

        // Сюда можно будет добавить /crm/auth/employee/signup, /crm/auth/employee/approve-device и т.д.
    }
}
