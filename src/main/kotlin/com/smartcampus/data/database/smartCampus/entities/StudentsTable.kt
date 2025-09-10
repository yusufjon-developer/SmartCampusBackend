package com.smartcampus.data.database.smartCampus.entities

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.javatime.date

object StudentsTable : IntIdTable("SmartCampus.dbo.Students") {
    val surname = varchar("surname", 255).nullable()
    val name = varchar("name", 255).nullable()
    val lastname = varchar("lastname", 255).nullable()
    val birthday = date("birthday").nullable()
    val groupId = reference("group_id", GroupsTable).nullable()
    val phoneNumber = varchar("phone_number", 50).nullable()
    val photo = blob("photo").nullable()
}