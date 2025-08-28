package com.smartcampus.features.auth

import com.smartcampus.app.utils.postWithAccess
import com.smartcampus.domain.models.auth.RegisterRequest
import com.smartcampus.domain.models.employee.EmployeeSignInRequest
import com.smartcampus.domain.models.student.StudentSignInRequest
import com.smartcampus.domain.security.models.Permissions
import com.smartcampus.domain.utils.Either
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(authService: AuthService) {

    route("/auth") {
        post("/student/signin") {
            try {
                val request = call.receive<StudentSignInRequest>()
                val response = authService.signInStudent(request)
                call.respond(HttpStatusCode.OK, response)
            } catch (e: ContentTransformationException) {
                application.log.warn(
                    "Student sign-in failed: Invalid request body. ${e.localizedMessage}",
                    e
                )
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Invalid request body: ${e.localizedMessage}")
                )
            } catch (e: SecurityException) {
                application.log.warn(
                    "Student sign-in failed: Unauthorized. ${e.localizedMessage}",
                    e
                )
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to e.localizedMessage))
            } catch (e: IllegalArgumentException) {
                application.log.warn(
                    "Student sign-in failed: Conflict/Bad Data. ${e.localizedMessage}",
                    e
                )
                call.respond(HttpStatusCode.Conflict, mapOf("error" to e.localizedMessage))
            } catch (e: Exception) {
                application.log.error("Failed to sign in student due to an unexpected error.", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "An unexpected error occurred.")
                )
            }
        }

        authenticate("auth-jwt") {
            postWithAccess("/student/signup", Permissions.STUDENTS_CREATE) {
                try {
                    val request = call.receive<RegisterRequest>()
                    when (val res = authService.register(request)) {
                        is Either.Left -> call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to res.value)
                        )

                        is Either.Right -> call.respond(HttpStatusCode.Created, res.value)
                    }
                } catch (e: ContentTransformationException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Invalid request body: ${e.message}")
                    )
                } catch (e: Exception) {
                    call.application.log.error("Registration error", e)
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Registration failed")
                    )
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
                application.log.warn(
                    "Employee sign-in failed: Invalid request body. ${e.localizedMessage}",
                    e
                )
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Invalid request body: ${e.localizedMessage}")
                )
            } catch (e: SecurityException) {
                application.log.warn(
                    "Employee sign-in failed: Unauthorized. ${e.localizedMessage}",
                    e
                )
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to e.localizedMessage))
            } catch (e: IllegalArgumentException) {
                application.log.warn(
                    "Employee sign-in failed: Conflict/Bad Data. ${e.localizedMessage}",
                    e
                )
                call.respond(HttpStatusCode.Conflict, mapOf("error" to e.localizedMessage))
            } catch (e: Exception) {
                application.log.error("Failed to sign in employee due to an unexpected error.", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "An unexpected error occurred.")
                )
            }
        }

        authenticate("auth-jwt") {
            postWithAccess("/employee/signup", Permissions.TEACHERS_CREATE) {
                try {
                    val request = call.receive<RegisterRequest>()
                    when (val res = authService.register(request)) {
                        is Either.Left -> call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to res.value)
                        )

                        is Either.Right -> call.respond(HttpStatusCode.Created, res.value)
                    }
                } catch (e: ContentTransformationException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Invalid request body: ${e.message}")
                    )
                } catch (e: Exception) {
                    call.application.log.error("Registration error", e)
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Registration failed")
                    )
                }
            }
        }
    }
}
