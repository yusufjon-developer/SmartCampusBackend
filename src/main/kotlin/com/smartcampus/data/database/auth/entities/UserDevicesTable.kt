package com.smartcampus.data.database.auth.entities

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.javatime.CurrentDateTime
import org.jetbrains.exposed.v1.javatime.datetime

object UserDevicesTable : IntIdTable("SmartCampusAuth.dbo.UserDevices") {
    val userId = reference("user_id", UsersTable)
    val deviceUuid = varchar("device_uuid", 255)
    val isApproved = bool("is_approved").default(false)
    val description = varchar("description", 255).nullable()
    val lastLoginAt = datetime("last_login_at").nullable()
    val registeredAt = datetime("registered_at").defaultExpression(CurrentDateTime)
    val approvedAt = datetime("approved_at").nullable()
    val approvedBy = reference("approved_by", UsersTable).nullable()

    init {
        uniqueIndex("UX_UserDeviceUUID", userId, deviceUuid)
    }
}