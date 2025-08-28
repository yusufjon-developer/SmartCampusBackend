package com.smartcampus.features.systemAdmin

import com.smartcampus.domain.models.UpdatePermissionsRequest
import com.smartcampus.domain.models.UpdateUserRequest
import com.smartcampus.domain.models.systemAdmin.RoleRequest
import com.smartcampus.features.common.getPageRequestParams
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

data class RawQueryPayload(val query: String)

fun Route.systemAdminRoutes(service: SystemAdminService) {

    // Защищаем весь блок /system-admin аутентификацией для администраторов

    route("/crm/system-admin") {

        // Вспомогательная функция для обработки ошибок остается такой же
        suspend fun ApplicationCall.handleAdminError(e: Throwable, action: String) {
            when (e) {
                is ContentTransformationException -> {
                    application.log.warn("Admin $action failed: Invalid request body. ${e.localizedMessage}")
                    respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Invalid request body: ${e.localizedMessage}")
                    )
                }

                is NoSuchElementException -> {
                    application.log.warn("Admin $action failed: Resource not found. ${e.message}")
                    respond(
                        HttpStatusCode.NotFound,
                        mapOf("error" to (e.message ?: "Resource not found."))
                    )
                }

                is IllegalArgumentException -> {
                    application.log.warn("Admin $action failed: Invalid argument. ${e.message}")
                    respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to (e.message ?: "Invalid argument."))
                    )
                }

                is IllegalStateException -> {
                    application.log.warn("Admin $action failed: Conflict or invalid state. ${e.message}")
                    respond(
                        HttpStatusCode.Conflict,
                        mapOf(
                            "error" to (e.message
                                ?: "Operation resulted in a conflict or invalid state.")
                        )
                    )
                }

                is SecurityException -> { // Для специфических SecurityException из сервиса
                    application.log.warn("Admin $action failed: Security violation from service. ${e.message}")
                    respond(
                        HttpStatusCode.Forbidden,
                        mapOf("error" to (e.message ?: "Forbidden."))
                    )
                }

                else -> {
                    application.log.error("Admin $action failed unexpectedly.", e)
                    respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "An unexpected error occurred during $action.")
                    )
                }
            }
        }

        // --- Roles Endpoints ---
        get("/roles") {
            try {
                val params = call.getPageRequestParams()
                val roles = service.getAllRoles(params)
                call.respond(HttpStatusCode.OK, roles)
            } catch (e: Exception) {
                call.handleAdminError(e, "get roles")
            }
        }

        post("/roles") {
            try {
                val request = call.receive<RoleRequest>()
                val newRole = service.createRole(request)
                call.respond(HttpStatusCode.Created, newRole)
            } catch (e: Exception) {
                call.handleAdminError(e, "create role")
            }
        }

        get("/roles/{id}") {
            val roleId = call.parameters["id"]?.toIntOrNull()
            if (roleId == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid role ID format."))
                return@get
            }
            try {
                val roleWithPermissionDetails = service.getRoleWithPermissions(roleId)
                call.respond(HttpStatusCode.OK, roleWithPermissionDetails)
            } catch (e: Exception) {
                call.handleAdminError(e, "get role by id")
            }
        }

        post("/roles/{id}/permissions") {
            val roleId = call.parameters["id"]?.toIntOrNull()
            val principal = call.principal<JWTPrincipal>()

            if (roleId == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid role ID format."))
                return@post
            }
            if (principal == null) {
                application.log.error("CRITICAL: Principal is null within auth-jwt-admin block for POST /roles/{id}/permissions.")
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Internal authentication error: Principal not found.")
                )
                return@post
            }

            try {
                val request = call.receive<UpdatePermissionsRequest>()
                service.updateRolePermissions(roleId, request, principal)
                val updated = service.getRoleWithPermissions(roleId)
                call.respond(HttpStatusCode.OK, updated)
            } catch (e: Exception) {
                call.handleAdminError(e, "update role permissions for role $roleId")
            }
        }

        delete("/roles/{id}") {
            val roleId = call.parameters["id"]?.toIntOrNull()
            if (roleId == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Invalid role ID format.")
                )
                return@delete
            }
            try {
                service.deleteRole(roleId)
                call.respond(HttpStatusCode.NoContent)
            } catch (e: Exception) {
                call.handleAdminError(e, "delete role")
            }
        }

        // --- Permissions Endpoints ---
        get("/permissions") {
            try {
                val params = call.getPageRequestParams()
                val permissions = service.getAllPermissions(params)
                call.respond(HttpStatusCode.OK, permissions)
            } catch (e: Exception) {
                call.handleAdminError(e, "get permissions")
            }
        }

        get("/permissions/{id}") {
            val permissionId = call.parameters["id"]?.toIntOrNull()
            if (permissionId == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Invalid permission ID format.")
                )
                return@get
            }
            try {
                val permission = service.getPermission(permissionId)
                call.respond(HttpStatusCode.OK, permission)
            } catch (e: Exception) {
                call.handleAdminError(e, "get permission by id")
            }
        }

        // --- User Permissions Endpoints ---
        get("users") {
            try {
                val params = call.getPageRequestParams()
                val users = service.getAllUsers(params)
                call.respond(HttpStatusCode.OK, users)
            } catch (e: Exception) {
                call.handleAdminError(e, "get roles")
            }
        }

        get("/users/{userId}") {
            val targetUserId = call.parameters["userId"]?.toIntOrNull()
            if (targetUserId == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Invalid target User ID format.")
                )
                return@get
            }
            try {
                val userPermissions = service.getUserPermissionsDetails(targetUserId)
                call.respond(HttpStatusCode.OK, userPermissions)
            } catch (e: Exception) {
                call.handleAdminError(e, "get user permissions details for user $targetUserId")
            }
        }

        put("/users/{id}") {
            val userId = call.parameters["id"]?.toIntOrNull()
            val principal = call.principal<JWTPrincipal>()
            if (userId == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid user id"))
                return@put
            }
            if (principal == null) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Internal authentication error: Principal not found."))
                return@put
            }

            try {
                val request = call.receive<UpdateUserRequest>()
                service.updateUserComposite(userId, request, principal)

                // вернём актуальные детали пользователя (permissions + devices)
                val updated = service.getUserPermissionsDetails(userId)
                call.respond(HttpStatusCode.OK, updated)
            } catch (e: Exception) {
                call.handleAdminError(e, "update user $userId")
            }
        }


        // --- Raw SQL Query Endpoint ---
        // Он также будет защищен `auth-jwt-admin`, так что пользователь УЖЕ будет SystemAdmin.
        // Внутренняя проверка в сервисе `executeRawQuery` остается как дополнительный уровень
        // или для специфической логики (например, если только определенный SystemAdmin с ID=1 может это делать).
        post("/raw-query") {
            // principal здесь будет JWTPrincipal, так как мы в authenticate("auth-jwt-admin")
            val principal = call.principal<JWTPrincipal>()

            // Эта проверка на null становится менее критичной, так как authenticate должен отсечь
            // неаутентифицированных пользователей. Но для безопасности можно оставить.
            if (principal == null) {
                // Это состояние не должно достигаться, если 'auth-jwt-admin' настроен правильно
                // и validate не вернул null по другой причине, кроме отсутствия прав.
                application.log.error("CRITICAL: Principal is null within auth-jwt-admin block for /raw-query. This should not happen.")
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Internal authentication error.")
                )
                return@post
            }

            try {
                val request = call.receive<RawQueryPayload>()
                // Передаем principal в сервис. Сервис может использовать его для логгирования
                // или для еще более гранулярных проверок, если это необходимо.
                val result = service.executeRawQuery(request.query, principal)
                call.respond(HttpStatusCode.OK, mapOf("result" to result))
            } catch (e: Exception) {
                call.handleAdminError(e, "execute raw query")
            }
        }
    }
}

