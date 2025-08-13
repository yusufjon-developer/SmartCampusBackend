package com.smartcampus.domain.utils

sealed class DomainError(open val message: String) {
    data class NotFound(
        val entityType: String,
        val entityId: Any? = null,
        val details: String = "Resource not found"
    ) : DomainError(details)

    data class Validation(
        val errors: Map<String, List<String>>,
        val summary: String = "Input validation failed"
    ) : DomainError(summary)

    data class DatabaseError(
        val reason: String,
        val cause: Throwable? = null
    ) : DomainError(reason)

    data class ConcurrencyError(
        val details: String = "Concurrency conflict detected"
    ) : DomainError(details)

    data class Unexpected(
        val details: String = "An unexpected error occurred",
        val cause: Throwable? = null
    ) : DomainError(details)

    data class Unauthorized(
        val details: String = "Authentication required or token is invalid"
    ) : DomainError(details)

    data class Forbidden(
        val details: String = "You do not have sufficient permissions to perform this action"
    ) : DomainError(details)

    data class UserAlreadyExists(
        val username: String,
        val details: String = "User with username '$username' already exists"
    ) : DomainError(details)

    data class RoleInUse(
        val roleName: String,
        val details: String = "Role '$roleName' is currently in use and cannot be deleted"
    ) : DomainError(details)

    data class InvalidCredentials(
        val details: String = "Invalid username or password"
    ) : DomainError(details)

    data class Custom(override val message: String) : DomainError(message)
}
