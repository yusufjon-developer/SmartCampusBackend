package com.smartcampus.features.auth

import com.smartcampus.domain.models.employee.EmployeeSignInRequest
import com.smartcampus.domain.models.employee.EmployeeSignUpRequest
import com.smartcampus.domain.models.security.Permissions
import com.smartcampus.domain.models.student.StudentSignInRequest
import com.smartcampus.domain.models.student.StudentSignUpRequest
import com.smartcampus.domain.security.AccessControlService
import com.smartcampus.domain.security.models.UserSessionPrincipal
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.log
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.ContentTransformationException
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.authRoutes(
    authService: AuthService,
    accessControlService: AccessControlService
) {

    route("/auth") {
        post("/student/signin") {
            try {
                val request = call.receive<StudentSignInRequest>()
                val response = authService.signInStudent(request)
                call.respond(HttpStatusCode.OK, response)
            } catch (e: ContentTransformationException) {
                application.log.warn("Student sign-in failed: Invalid request body. ${e.localizedMessage}", e)
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request body: ${e.localizedMessage}"))
            } catch (e: SecurityException) {
                application.log.warn("Student sign-in failed: Unauthorized. ${e.localizedMessage}", e)
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to e.localizedMessage))
            } catch (e: IllegalArgumentException) {
                application.log.warn("Student sign-in failed: Conflict/Bad Data. ${e.localizedMessage}", e)
                call.respond(HttpStatusCode.Conflict, mapOf("error" to e.localizedMessage))
            } catch (e: Exception) {
                application.log.error("Failed to sign in student due to an unexpected error.", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "An unexpected error occurred."))
            }
        }

        authenticate("auth-jwt") {
            post("/student/signup") {
                if (!accessControlService.hasAccess(call, Permissions.STUDENT_REGISTER)) {
                    return@post
                }
                try {
                    val request = call.receive<StudentSignUpRequest>()
                    if (request.username.isBlank() || request.email.isBlank() || request.password.isBlank()) {
                        application.log.warn("Student sign-up attempt with blank fields by user: ${call.principal<UserSessionPrincipal>()?.username}")
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Username, email, and password cannot be empty."))
                        return@post
                    }
                    val response = authService.signUpStudent(request)
                    call.respond(HttpStatusCode.Created, response)
                } catch (e: ContentTransformationException) {
                    application.log.warn("Student sign-up failed: Invalid request body. ${e.localizedMessage}", e)
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request body: ${e.localizedMessage}"))
                } catch (e: IllegalArgumentException) {
                    application.log.warn("Student sign-up failed: Conflict/Bad Data. ${e.localizedMessage}", e)
                    call.respond(HttpStatusCode.Conflict, mapOf("error" to e.localizedMessage))
                } catch (e: IllegalStateException) {
                    application.log.error("Configuration error during student sign up.", e)
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Server configuration error: ${e.localizedMessage}"))
                } catch (e: Exception) {
                    application.log.error("Failed to sign up student due to an unexpected error.", e)
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "An unexpected error occurred."))
                }
            }
        }
    }

    route("/crm/auth") {
        post("/employee/signin") {
            try {
                val request = call.receive<EmployeeSignInRequest>()
                val response = authService.signInEmployee(request)
                call.respond(HttpStatusCode.OK, response)
            } catch (e: ContentTransformationException) {
                application.log.warn("Employee sign-in failed: Invalid request body. ${e.localizedMessage}", e)
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request body: ${e.localizedMessage}"))
            } catch (e: SecurityException) {
                application.log.warn("Employee sign-in failed: Unauthorized. ${e.localizedMessage}", e)
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to e.localizedMessage))
            } catch (e: IllegalArgumentException) {
                application.log.warn("Employee sign-in failed: Conflict/Bad Data. ${e.localizedMessage}", e)
                call.respond(HttpStatusCode.Conflict, mapOf("error" to e.localizedMessage))
            } catch (e: Exception) {
                application.log.error("Failed to sign in employee due to an unexpected error.", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "An unexpected error occurred."))
            }
        }

        authenticate("auth-jwt") {
            post("/employee/signup") {
                if (!accessControlService.hasAccess(call, Permissions.EMPLOYEE_REGISTER)) {
                    return@post
                }
                try {
                    val request = call.receive<EmployeeSignUpRequest>()
                    if (request.username.isBlank() || request.email.isBlank() || request.password.isBlank() || request.roleName.isBlank()) {
                        application.log.warn("Employee sign-up attempt with blank fields by user: ${call.principal<UserSessionPrincipal>()?.username}")
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Username, email, password, and role name cannot be empty."))
                        return@post
                    }
                    val response = authService.signUpEmployee(request)
                    call.respond(HttpStatusCode.Created, response)
                } catch (e: ContentTransformationException) {
                    application.log.warn("Employee sign-up failed: Invalid request body. ${e.localizedMessage}", e)
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request body: ${e.localizedMessage}"))
                } catch (e: IllegalArgumentException) {
                    application.log.warn("Employee sign-up failed: Conflict/Bad Data. ${e.localizedMessage}", e)
                    call.respond(HttpStatusCode.Conflict, mapOf("error" to e.localizedMessage))
                } catch (e: IllegalStateException) {
                    application.log.error("Configuration error during employee sign up.", e)
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Server configuration error: ${e.localizedMessage}"))
                } catch (e: Exception) {
                    application.log.error("Failed to sign up employee due to an unexpected error.", e)
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "An unexpected error occurred."))
                }
            }
        }
    }
}