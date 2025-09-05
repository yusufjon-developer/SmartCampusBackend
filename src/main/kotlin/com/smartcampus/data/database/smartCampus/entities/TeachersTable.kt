package com.smartcampus.data.database.smartCampus.entities

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.javatime.date

object TeachersTable : IntIdTable("SmartCampus.dbo.Teachers") {
    val surname = varchar("surname", 255).nullable()
    val name = varchar("name", 255).nullable()
    val lastname = varchar("lastname", 255).nullable()
    val birthday = date("birthday").nullable()
    val phoneNumber = varchar("phone_number", 50).nullable()
    val photo = blob("photo").nullable()
}