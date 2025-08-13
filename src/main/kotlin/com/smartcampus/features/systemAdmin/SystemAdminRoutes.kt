package com.smartcampus.features.systemAdmin

import com.smartcampus.domain.models.systemAdmin.PermissionRequest
import com.smartcampus.domain.models.systemAdmin.RoleRequest
import com.smartcampus.domain.models.systemAdmin.UpdateUserPermissionsRequest
import com.smartcampus.features.common.getPageRequestParams
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.log
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.ContentTransformationException
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

data class AssignPermissionToRolePayload(val permissionId: Int)
data class RawQueryPayload(val query: String)



fun Route.systemAdminRoutes(service: SystemAdminService) {

    // Защищаем весь блок /system-admin аутентификацией для администраторов
    authenticate("auth-jwt-admin") {
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
                            mapOf("error" to (e.message ?: "Operation resulted in a conflict or invalid state."))
                        )
                    }
                    is SecurityException -> { // Для специфических SecurityException из сервиса
                        application.log.warn("Admin $action failed: Security violation from service. ${e.message}")
                        respond(HttpStatusCode.Forbidden, mapOf("error" to (e.message ?: "Forbidden.")))
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

            get("/roles/{id}") {
                val roleId = call.parameters["id"]?.toIntOrNull()
                if (roleId == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid role ID format."))
                    return@get
                }
                try {
                    val roleWithPermissions = service.getRoleWithPermissions(roleId)
                    call.respond(HttpStatusCode.OK, roleWithPermissions)
                } catch (e: Exception) {
                    call.handleAdminError(e, "get role by id")
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

            delete("/roles/{id}") {
                val roleId = call.parameters["id"]?.toIntOrNull()
                if (roleId == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid role ID format."))
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
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid permission ID format."))
                    return@get
                }
                try {
                    val permission = service.getPermission(permissionId)
                    call.respond(HttpStatusCode.OK, permission)
                } catch (e: Exception) {
                    call.handleAdminError(e, "get permission by id")
                }
            }

            post("/permissions") {
                try {
                    val request = call.receive<PermissionRequest>()
                    val newPermission = service.createPermission(request)
                    call.respond(HttpStatusCode.Created, newPermission)
                } catch (e: Exception) {
                    call.handleAdminError(e, "create permission")
                }
            }

            delete("/permissions/{id}") {
                val permissionId = call.parameters["id"]?.toIntOrNull()
                if (permissionId == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid permission ID format."))
                    return@delete
                }
                try {
                    service.deletePermission(permissionId)
                    call.respond(HttpStatusCode.NoContent)
                } catch (e: Exception) {
                    call.handleAdminError(e, "delete permission")
                }
            }

            // --- Role-Permission Links Endpoints ---
            post("/roles/{roleId}/permissions") {
                val roleId = call.parameters["roleId"]?.toIntOrNull()
                if (roleId == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid role ID format."))
                    return@post
                }
                try {
                    val payload = call.receive<AssignPermissionToRolePayload>()
                    service.assignPermissionToRole(roleId, payload.permissionId)
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Permission assigned successfully."))
                } catch (e: Exception) {
                    call.handleAdminError(e, "assign permission to role")
                }
            }

            delete("/roles/{roleId}/permissions/{permissionId}") {
                val roleId = call.parameters["roleId"]?.toIntOrNull()
                val permissionId = call.parameters["permissionId"]?.toIntOrNull()
                if (roleId == null || permissionId == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid role or permission ID format."))
                    return@delete
                }
                try {
                    service.revokePermissionFromRole(roleId, permissionId)
                    call.respond(HttpStatusCode.NoContent)
                } catch (e: Exception) {
                    call.handleAdminError(e, "revoke permission from role")
                }
            }

            get("/users/{userId}/permissions") {
                val targetUserId = call.parameters["userId"]?.toIntOrNull()
                if (targetUserId == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid target User ID format."))
                    return@get
                }
                try {
                    val userPermissions = service.getUserPermissionsDetails(targetUserId)
                    call.respond(HttpStatusCode.OK, userPermissions)
                } catch (e: Exception) {
                    call.handleAdminError(e, "get user permissions details for user $targetUserId")
                }
            }

            post("/users/{userId}/permissions") {
                val targetUserId = call.parameters["userId"]?.toIntOrNull()
                val principal = call.principal<JWTPrincipal>()

                if (targetUserId == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid target User ID format."))
                    return@post
                }
                if (principal == null) {
                    application.log.error("CRITICAL: Principal is null within auth-jwt-admin block for POST /users/{userId}/permissions.")
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Internal authentication error: Principal not found."))
                    return@post
                }

                try {
                    val request = call.receive<UpdateUserPermissionsRequest>()
                    service.updateUserIndividualPermissions(targetUserId, request, principal)
                    call.respond(HttpStatusCode.OK, mapOf("message" to "User's individual permissions update process initiated successfully."))
                } catch (e: Exception) {
                    call.handleAdminError(e, "update user individual permissions for user $targetUserId")
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
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Internal authentication error."))
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
}
