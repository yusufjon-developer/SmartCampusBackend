package com.smartcampus.features.auth

import com.smartcampus.domain.models.auth.RegisterRequest
import com.smartcampus.domain.models.employee.EmployeeSignInRequest
import com.smartcampus.domain.security.models.Permissions
import com.smartcampus.domain.models.student.StudentSignInRequest
import com.smartcampus.domain.security.AccessControlService
import com.smartcampus.domain.utils.Either
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.log
import io.ktor.server.auth.authenticate
import io.ktor.server.request.ContentTransformationException
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.authRoutes(
    authService: AuthService,
    accessControlService: AccessControlService
) {

    route("/auth") {
        get("/student/signin") {
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
                if (!accessControlService.hasAccess(call, Permissions.STUDENTS_CREATE)) {
                    return@post
                }

                try {
                    val request = call.receive<RegisterRequest>()
                    when (val res = authService.register(request)) {
                        is Either.Left -> call.respond(HttpStatusCode.BadRequest, mapOf("error" to res.value))
                        is Either.Right -> call.respond(HttpStatusCode.Created, res.value)
                    }
                } catch (e: ContentTransformationException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request body: ${e.message}"))
                } catch (e: Exception) {
                    call.application.log.error("Registration error", e)
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Registration failed"))
                }
            }
        }
    }

    route("/crm/auth") {
        get("/employee/signin") {
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
                if (!accessControlService.hasAccess(call, Permissions.TEACHERS_CREATE)) {
                    return@post
                }
                try {
                    val request = call.receive<RegisterRequest>()
                    when (val res = authService.register(request)) {
                        is Either.Left -> call.respond(HttpStatusCode.BadRequest, mapOf("error" to res.value))
                        is Either.Right -> call.respond(HttpStatusCode.Created, res.value)
                    }
                } catch (e: ContentTransformationException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request body: ${e.message}"))
                } catch (e: Exception) {
                    call.application.log.error("Registration error", e)
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Registration failed"))
                }
            }
        }
    }
}