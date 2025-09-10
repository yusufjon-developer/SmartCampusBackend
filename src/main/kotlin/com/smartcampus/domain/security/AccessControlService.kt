package com.smartcampus.domain.security

import com.smartcampus.data.dao.SystemAdminDao
import com.smartcampus.domain.security.models.UserSessionPrincipal
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.slf4j.LoggerFactory

class AccessControlService(private val systemAdminDao: SystemAdminDao) {
    private val log = LoggerFactory.getLogger(AccessControlService::class.java)

    companion object {
        const val SYSTEM_ADMIN_ROLE_NAME = "SystemAdmin"
    }

    suspend fun hasAccess(
        call: ApplicationCall,
        vararg requiredPermissions: String
    ): Boolean {
        val principal = call.principal<UserSessionPrincipal>()

        if (principal == null) {
            log.warn(
                "AccessControl (hasAccess): No principal (user not authenticated) for call to ${call.request.uri}. " +
                        "Responding 401 Unauthorized."
            )
            call.respond(HttpStatusCode.Companion.Unauthorized, mapOf("error" to "Authentication required."))
            return false
        }

        if (principal.roleNames.any { it.equals(SYSTEM_ADMIN_ROLE_NAME, ignoreCase = true) }) {
            log.info(
                "AccessControl (hasAccess): GRANTED. User '${principal.username}' (ID: ${principal.userId}) " +
                        "is identified as a System Administrator (role: '$SYSTEM_ADMIN_ROLE_NAME'). " +
                        "Access granted by default for call to ${call.request.uri} " +
                        "(required permissions, if any: [${requiredPermissions.joinToString()}], were bypassed)."
            )
            return true
        }

        if (requiredPermissions.isEmpty()) {
            log.info(
                "AccessControl (hasAccess): No specific permissions required for user '${principal.username}' " +
                        "(ID: ${principal.userId}) for call to ${call.request.uri}. Access granted by default to authenticated (non-admin) user."
            )
            return true
        }

        val rolePermissionIds = principal.roleId?.let { userRoleId ->
            try {
                systemAdminDao.getPermissionIdsForRole(userRoleId)
            } catch (e: Exception) {
                log.error(
                    "AccessControl (hasAccess): Database error fetching permission IDs for role ID $userRoleId " +
                            "for user '${principal.username}' (ID: ${principal.userId}).", e
                )
                call.respond(
                    HttpStatusCode.Companion.InternalServerError,
                    mapOf("error" to "Error checking permissions due to a server issue.")
                )
                return false
            }
        } ?: emptySet()

        val individualPermissionIds = try {
            systemAdminDao.getIndividualPermissionIdsForUser(principal.userId)
        } catch (e: Exception) {
            log.error(
                "AccessControl (hasAccess): Database error fetching individual permission IDs " +
                        "for user '${principal.username}' (ID: ${principal.userId}).", e
            )
            call.respond(
                HttpStatusCode.Companion.InternalServerError,
                mapOf("error" to "Error checking permissions due to a server issue.")
            )
            return false
        }

        val allUserPermissionIds = rolePermissionIds + individualPermissionIds

        if (allUserPermissionIds.isEmpty()) {
            log.warn(
                "AccessControl (hasAccess): FORBIDDEN. User '${principal.username}' (ID: ${principal.userId}) has no " +
                        "permissions assigned (neither role-based nor individual). " +
                        "Required one of: [${requiredPermissions.joinToString()}] for call to ${call.request.uri}. " +
                        "Responding 403 Forbidden."
            )
            call.respond(
                HttpStatusCode.Companion.Forbidden,
                mapOf("error" to "You do not have sufficient permissions to perform this action.")
            )
            return false
        }

        val userPermissionNames = try {
            systemAdminDao.getPermissionNamesByIds(allUserPermissionIds)
        } catch (e: Exception) {
            log.error(
                "AccessControl (hasAccess): Database error fetching permission names by IDs " +
                        "for user '${principal.username}' (ID: ${principal.userId}). IDs: $allUserPermissionIds.", e
            )
            call.respond(
                HttpStatusCode.Companion.InternalServerError,
                mapOf("error" to "Error checking permissions due to a server issue.")
            )
            return false
        }

        val hasRequiredPermission = requiredPermissions.any { requiredPerm ->
            userPermissionNames.any { userPerm -> userPerm.equals(requiredPerm, ignoreCase = true) }
        }

        if (!hasRequiredPermission) {
            log.warn(
                "AccessControl (hasAccess): FORBIDDEN. User '${principal.username}' (ID: ${principal.userId}) " +
                        "for call to ${call.request.uri}. Required one of: [${requiredPermissions.joinToString()}], " +
                        "but user has only these effective permissions: [${userPermissionNames.joinToString()}]. " +
                        "Responding 403 Forbidden."
            )
            call.respond(
                HttpStatusCode.Companion.Forbidden,
                mapOf("error" to "You do not have sufficient permissions to perform this action.")
            )
            return false
        }

        log.info(
            "AccessControl (hasAccess): GRANTED. User '${principal.username}' (ID: ${principal.userId}) " +
                    "for call to ${call.request.uri}, as they possess at least one of the required " +
                    "permissions: [${requiredPermissions.joinToString()}]."
        )
        return true
    }
}
