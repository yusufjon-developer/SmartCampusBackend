package com.smartcampus.app.plugins

import com.smartcampus.domain.security.AccessControlService
import com.smartcampus.domain.security.models.UserSessionPrincipal
import com.smartcampus.features.auth.AuthService
import com.smartcampus.features.auth.authRoutes
import com.smartcampus.features.students.StudentsService
import com.smartcampus.features.students.studentsRoutes
import com.smartcampus.features.systemAdmin.SystemAdminService
import com.smartcampus.features.systemAdmin.systemAdminRoutes
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.log
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.util.AttributeKey
import org.koin.ktor.ext.inject

private val AccessControlKey = AttributeKey<AccessControlService>("AccessControlService")

val Application.accessControlService: AccessControlService
    get() = this.attributes.getOrNull(AccessControlKey)
        ?: error("AccessControlService is not registered in Application attributes")

fun Application.configureRouting() {

    val accessControlService by inject<AccessControlService>()
    val authService by inject<AuthService>()
    val systemAdminService by inject<SystemAdminService>()
    val studentsService by inject<StudentsService>()

    attributes.put(AccessControlKey, accessControlService)

    routing {
        authRoutes(authService)

        // в configureRouting / routing { authenticate("auth-jwt") { ... } }
        authenticate("auth-jwt") {
            route("/debug") {
                get("/me/permissions") {
                    val principal = call.principal<UserSessionPrincipal>()
                    if (principal == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "no principal"))
                        return@get
                    }
                    val systemAdminDao by inject<com.smartcampus.data.dao.SystemAdminDao>()
                    val rolePerms = principal.roleId?.let { systemAdminDao.getPermissionIdsForRole(it) } ?: emptySet()
                    val indivPerms = systemAdminDao.getIndividualPermissionIdsForUser(principal.userId)
                    val all = rolePerms + indivPerms
                    val names = systemAdminDao.getPermissionNamesByIds(all)
                    call.respond(
                        mapOf(
                            "principal" to mapOf("userId" to principal.userId, "username" to principal.username, "roleNames" to principal.roleNames, "roleId" to principal.roleId),
                            "rolePermissionIds" to rolePerms,
                            "individualPermissionIds" to indivPerms,
                            "allPermissionIds" to all,
                            "permissionNames" to names
                        )
                    )
                }
            }

            // ваши реальные маршруты
            studentsRoutes(studentsService)
        }


        authenticate("auth-jwt-admin") {
            systemAdminRoutes(systemAdminService)
        }

        log.info("Routing configured.")
    }
}