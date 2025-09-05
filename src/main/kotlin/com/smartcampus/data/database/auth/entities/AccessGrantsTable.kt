package com.smartcampus.data.database.auth.entities

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.javatime.CurrentDateTime
import org.jetbrains.exposed.v1.javatime.datetime

object AccessGrantsTable : IntIdTable("SmartCampusAuth.dbo.Access_Grants") {
    val grantedBy = reference("granted_by", UsersTable.id)
    val grantedTo = reference("granted_to", UsersTable.id)
    val permissionId = reference("permission_id", PermissionsTable.id)
    val grantDate =
        datetime("grant_date").defaultExpression(CurrentDateTime)
    val expiresAt = datetime("expires_at").nullable()
    val comment = varchar("comment", 255).nullable()

    init {
        uniqueIndex("UX_GrantedIndividualPermission", grantedTo, permissionId)
    }
}