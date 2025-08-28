package com.smartcampus.features.systemAdmin

import com.smartcampus.domain.models.UserDto
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult
import com.smartcampus.domain.models.systemAdmin.*
import com.smartcampus.domain.repositories.SystemAdminRepository
import io.ktor.server.auth.jwt.*
import org.slf4j.LoggerFactory

class SystemAdminService(
    private val repository: SystemAdminRepository
) {
    private val log = LoggerFactory.getLogger(SystemAdminService::class.java)

    // --- Roles ---
    suspend fun getAllRoles(params: PageRequestParams): PaginatedResult<RoleResponse> {
        log.info("Service: Fetching roles with params: $params")
        return repository.getRoles(params)
    }

    suspend fun getRoleWithPermissions(roleId: Int): RolePermissionDetailsDto {
        log.debug("Service: Fetching role $roleId with permissions.")
        return repository.getRoleById(roleId)
            ?: throw NoSuchElementException("Role with id $roleId not found.")
    }

    suspend fun updateRolePermissions(roleId: Int, request: UpdatePermissionsRequest, performingAdminPrincipal: JWTPrincipal) {
        val performingAdminId = performingAdminPrincipal.payload.getClaim("userId").asInt()
            ?: throw IllegalStateException("Performing admin User ID not found in JWT principal.")
        val performingAdminUsername = performingAdminPrincipal.payload.getClaim("username").asString()
            ?: "UnknownAdmin"

        log.info("Service: Admin '$performingAdminUsername' (ID: $performingAdminId) is attempting to update permissions for role ID: $roleId. Request: $request")

        val success = repository.updateRolePermissions(roleId, request, performingAdminId)
        if (!success) {
            throw IllegalStateException("Failed to update one or more permissions for role $roleId. Check logs for details.")
        }
        log.info("Service: Successfully updated permissions for role $roleId by admin '$performingAdminUsername'.")
    }

    suspend fun createRole(request: RoleRequest): RoleResponse {
        log.info("Service: Creating role with name '${request.name}'.")
        if (request.name.isBlank()) {
            throw IllegalArgumentException("Role name cannot be empty.")
        }
        // Можно добавить проверку на уникальность имени здесь, если это критично для MVP
        // val existing = repository.getRoles().find { it.name == request.name }
        // if (existing != null) throw IllegalStateException("Role with name '${request.name}' already exists.")
        return repository.createRole(request)
    }

    suspend fun deleteRole(roleId: Int) {
        log.info("Service: Deleting role $roleId.")
        val success = repository.deleteRoleById(roleId)
        if (!success) {
            throw NoSuchElementException("Role with id $roleId not found or could not be deleted.")
        }
    }

    // --- Permissions ---
    suspend fun getAllPermissions(params: PageRequestParams): PaginatedResult<PermissionResponse> {
        log.info("Service: Fetching permissions with params: $params")
        return repository.getPermissions(params)
    }

    suspend fun getPermission(permissionId: Int): PermissionResponse {
        log.debug("Service: Fetching permission $permissionId.")
        return repository.getPermissionsById(permissionId)
            ?: throw NoSuchElementException("Permission with id $permissionId not found.")
    }

    suspend fun getAllUsers(params: PageRequestParams): PaginatedResult<UserDto> {
        log.info("Service: Fetching all users.")
        return repository.getUsers(params)
    }
    suspend fun getUserPermissionsDetails(userId: Int): UserPermissionDetailsDto {
        log.debug("Service: Fetching permission details for user $userId.")
        return repository.getUserPermissionsDetails(userId)
            ?: throw NoSuchElementException("User with ID $userId not found or details could not be retrieved.")
    }

    suspend fun updateUserIndividualPermissions(targetUserId: Int, request: UpdatePermissionsRequest, performingAdminPrincipal: JWTPrincipal) {
        val performingAdminId = performingAdminPrincipal.payload.getClaim("userId").asInt()
            ?: throw IllegalStateException("Performing admin User ID not found in JWT principal.")
        val performingAdminUsername = performingAdminPrincipal.payload.getClaim("username").asString()
            ?: "UnknownAdmin"

        log.info("Service: Admin '$performingAdminUsername' (ID: $performingAdminId) is attempting to update individual permissions for user ID: $targetUserId. Request: $request")

        if (targetUserId == performingAdminId) {
            // Предосторожность: админ пытается отозвать права у самого себя.
            // Можно добавить более сложную логику, если есть критичные "само-админские" права.
            log.warn("Service: Admin '$performingAdminUsername' is attempting to modify their own individual permissions. Proceeding with caution.")
        }

        // Дополнительные бизнес-проверки можно добавить здесь, если необходимо
        // Например, не позволять отзывать определенные базовые разрешения и т.д.

        val success = repository.updateUserIndividualPermissions(targetUserId, request, performingAdminId)
        if (!success) {
            // Репозиторий вернет false, если, например, не удалось выдать какое-то право из-за его отсутствия
            // или если целевой пользователь не найден (хотя это проверяется в репозитории).
            throw IllegalStateException("Failed to update one or more individual permissions for user $targetUserId. Check logs for details.")
        }
        log.info("Service: Successfully initiated update for individual permissions for user $targetUserId by admin '$performingAdminUsername'.")
    }

    suspend fun revokePermissionFromRole(roleId: Int, permissionId: Int) {
        log.info("Service: Revoking permission $permissionId from role $roleId.")

        // 1. Проверить, существует ли роль (опционально, но хорошо для консистентности)
        repository.getRoleById(roleId)
            ?: throw NoSuchElementException("Role with id $roleId not found. Cannot revoke permission.")

        // 2. Проверить, существует ли разрешение (опционально)
        repository.getPermissionsById(permissionId)
            ?: throw NoSuchElementException("Permission with id $permissionId not found. Cannot revoke from role.")

        // 3. Попытаться удалить связь
        val success = repository.revokePermissionFromRole(roleId, permissionId)
        if (!success) {
            // Если success = false, это означает, что связь не была найдена для удаления.
            // Это не обязательно ошибка, если операция идемпотентна.
            // Но для учебного проекта, если мы хотим быть строгими и сообщать, что ничего не было удалено:
            throw NoSuchElementException("Link between role $roleId and permission $permissionId not found. Nothing to revoke.")
        }
        log.info("Successfully revoked permission $permissionId from role $roleId.")
    }

    // --- Raw SQL Query ---
    suspend fun executeRawQuery(query: String, performingUserPrincipal: JWTPrincipal /* Передаем Principal для извлечения информации о пользователе */): String {
        // Извлекаем ID пользователя и/или другие атрибуты для проверки прав
        // Это пример, адаптируйте под вашу модель Principal (например, JWTPrincipal)
        val performingUserId = performingUserPrincipal.payload.subject?.toIntOrNull()
        val username = performingUserPrincipal.payload.getClaim("username")?.asString()

        log.warn("Service: User '$username' (ID: $performingUserId) attempting to execute raw query: $query")

        // !!! КРИТИЧЕСКАЯ ПРОВЕРКА БЕЗОПАСНОСТИ !!!
        // Это должно быть реальной проверкой, а не просто по ID=1
        // Например, проверка специальной роли "SUPER_ADMIN" или флага в профиле пользователя.
        // val isSuperUser = authRepository.isUserSuperAdmin(performingUserId) // Предполагаемый метод

        // Упрощенная проверка для учебного проекта (НО ОПАСНАЯ ДЛЯ PROD)
        val isAllowedToExecuteRawQuery = (username == "sudo" || performingUserId == 1) // Пример

        if (!isAllowedToExecuteRawQuery) {
            log.error("CRITICAL: User '$username' (ID: $performingUserId) is NOT AUTHORIZED and attempted to execute raw query.")
            throw SecurityException("User '$username' is not authorized to execute raw SQL queries.")
        }

        if (query.isBlank()) {
            throw IllegalArgumentException("Raw query cannot be empty.")
        }

        // Дополнительная (очень сложная для полной реализации) проверка:
        // Попытаться определить тип запроса (SELECT, UPDATE, DDL и т.д.)
        // и, возможно, запретить некоторые из них, если политика безопасности это требует.
        // Например, запретить DROP TABLE, DELETE без WHERE и т.д.
        // Это очень сложно сделать надежно для произвольного SQL.
        val lowerCaseQuery = query.trim().lowercase()
        if (lowerCaseQuery.startsWith("drop ") || lowerCaseQuery.startsWith("truncate ") || lowerCaseQuery.startsWith("delete from ") && !lowerCaseQuery.contains(" where ")) {
            log.error("CRITICAL: Potentially DANGEROUS raw query detected from user '$username': $query")
            // AccessControlService зависимости от политики, можно либо просто логировать и разрешать (если sudo доверяют),
            // либо кидать исключение.
            // throw SecurityException("Execution of potentially destructive DDL/DML (DROP, TRUNCATE, DELETE without WHERE) via raw query is restricted for safety.")
        }


        log.info("User '$username' (ID: $performingUserId) is authorized. Executing raw query.")
        return repository.sendQuery(query)
    }
}
