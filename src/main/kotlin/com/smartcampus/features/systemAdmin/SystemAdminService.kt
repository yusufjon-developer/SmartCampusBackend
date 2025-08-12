package com.smartcampus.features.systemAdmin

import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult
import com.smartcampus.domain.models.systemAdmin.PermissionRequest
import com.smartcampus.domain.models.systemAdmin.PermissionResponse
import com.smartcampus.domain.models.systemAdmin.RoleRequest
import com.smartcampus.domain.models.systemAdmin.RoleResponse
import com.smartcampus.domain.models.systemAdmin.RoleWithPermissionsResponse
import com.smartcampus.domain.repositories.SystemAdminRepository
import io.ktor.server.auth.jwt.JWTPrincipal
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

    suspend fun getRoleWithPermissions(roleId: Int): RoleWithPermissionsResponse {
        log.debug("Service: Fetching role $roleId with permissions.")
        return repository.getRoleById(roleId)
            ?: throw NoSuchElementException("Role with id $roleId not found.")
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

    suspend fun createPermission(request: PermissionRequest): PermissionResponse {
        log.info("Service: Creating permission with name '${request.name}'.")
        if (request.name.isBlank()) {
            throw IllegalArgumentException("Permission name cannot be empty.")
        }
        return repository.createPermissions(request)
    }

    suspend fun deletePermission(permissionId: Int) {
        log.info("Service: Deleting permission $permissionId.")
        val success = repository.deletePermissionsById(permissionId)
        if (!success) {
            throw NoSuchElementException("Permission with id $permissionId not found or could not be deleted.")
        }
    }

    suspend fun assignPermissionToRole(roleId: Int, permissionId: Int) {
        log.info("Service: Assigning permission $permissionId to role $roleId.")

        // 1. Проверить, существует ли роль
        repository.getRoleById(roleId)
            ?: throw NoSuchElementException("Role with id $roleId not found. Cannot assign permission.")

        // 2. Проверить, существует ли разрешение
        repository.getPermissionsById(permissionId)
            ?: throw NoSuchElementException("Permission with id $permissionId not found. Cannot assign to role.")

        // 3. Попытаться добавить связь (репозиторий должен быть идемпотентным или обрабатывать дубликаты)
        val success = repository.assignPermissionToRole(roleId, permissionId)
        if (!success) {
            // Если репозиторий возвращает false при существующей связи, это нормально.
            // Если он возвращает false по другой причине (например, внутренняя ошибка БД),
            // то это уже проблема. Для учебного проекта можно предположить,
            // что false здесь означает, что связь УЖЕ СУЩЕСТВОВАЛА и не была создана заново,
            // либо возникла другая проблема, не связанная с отсутствием роли/разрешения (они проверены).
            // Для большей точности, метод репозитория assignPermissionToRole мог бы возвращать
            // enum или код результата (CREATED, ALREADY_EXISTS, FAILED_OTHER).
            // В текущей реализации с boolean:
            log.warn("repository.assignPermissionToRole returned false for role $roleId, permission $permissionId. This might indicate the link already exists or an issue if the DB doesn't handle duplicates gracefully.")
            // Можно не кидать исключение, если "уже существует" - это не ошибка для вас.
            // Если же вы хотите, чтобы метод был строгим и падал, если связь уже есть:
            // (потребовался бы метод repository.rolePermissionLinkExists(roleId, permissionId))
            // if (repository.rolePermissionLinkExists(roleId, permissionId)) {
            //     throw IllegalStateException("Permission $permissionId is already assigned to role $roleId.")
            // } else {
            //     throw IllegalStateException("Failed to assign permission $permissionId to role $roleId due to an unexpected issue after checks.")
            // }
            // Для упрощения, если repository.assignPermissionToRole кидает исключение при дубликате (например, PK violation),
            // то этот блок if(!success) может не понадобиться или будет обрабатывать другие редкие случаи.
            // Если ваш DAO.assignPermissionToRole уже обрабатывает дубликаты и возвращает true/false
            // (false если дубликат), то здесь можно ничего не делать или логировать.
            // Если DAO просто делает INSERT и полагается на исключение БД при дубликате, то
            // Ktor StatusPages или try-catch в роуте поймают это исключение (часто PSQLException или аналог).
            // Давайте предположим, что repository.assignPermissionToRole возвращает false, если связь не была СОЗДАНА (т.е. уже есть или ошибка)
            // И мы хотим быть строгими: если не создана, и это не из-за отсутствия роли/права (проверили), то это проблема.
            throw IllegalStateException("Failed to create the link between role $roleId and permission $permissionId. The link might already exist or another issue occurred.")
        }
        log.info("Successfully assigned permission $permissionId to role $roleId.")
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
            // В зависимости от политики, можно либо просто логировать и разрешать (если sudo доверяют),
            // либо кидать исключение.
            // throw SecurityException("Execution of potentially destructive DDL/DML (DROP, TRUNCATE, DELETE without WHERE) via raw query is restricted for safety.")
        }


        log.info("User '$username' (ID: $performingUserId) is authorized. Executing raw query.")
        return repository.sendQuery(query)
    }
}
