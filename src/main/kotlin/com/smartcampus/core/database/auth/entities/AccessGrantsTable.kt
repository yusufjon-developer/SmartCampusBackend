package com.smartcampus.core.database.auth.entities

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.javatime.CurrentDateTime
import org.jetbrains.exposed.v1.javatime.datetime

object AccessGrantsTable : IntIdTable("Access_Grants") {
    val grantedBy = integer("granted_by").references(UsersTable.id)
    val grantedTo = integer("granted_to").references(UsersTable.id)
    val permissionId = integer("permission_id").references(PermissionsTable.id)
    val grantDate = datetime("grant_date").defaultExpression(CurrentDateTime)
    val expiresAt = datetime("expires_at").nullable()
    val comment = varchar("comment", 255).nullable()

    // Дополнительное ограничение: один пользователь не может получить одно и то же право дважды (активное).
    // Это сложнее реализовать на уровне Exposed Table DSL, если считать только активные.
    // Если просто уникальность пары (grantedTo, permissionId), то можно добавить:
    // init {
    //     uniqueIndex(grantedTo, permissionId)
    // }
    // Если логика сложнее (например, нет дубликатов с expires_at > NOW() или NULL),
    // то это лучше проверять на уровне бизнес-логики (сервисов) перед вставкой.
    // На уровне БД можно создать уникальный индекс с фильтром (filtered unique index), если СУБД поддерживает.
}