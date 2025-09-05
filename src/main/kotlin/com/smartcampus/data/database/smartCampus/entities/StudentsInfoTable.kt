package com.smartcampus.data.database.smartCampus.entities

import org.jetbrains.exposed.v1.core.Table

object StudentsInfoTable : Table("SmartCampus.dbo.Students_Info") {
    val studentId = reference("student_id", StudentsTable)
    override val primaryKey = PrimaryKey(studentId)

    val address = text("address").nullable()
    val passportNumber = varchar("passport_number", 100).nullable()
    val school = varchar("school", 255).nullable()
    val documentNumber = varchar("document_number", 100).nullable()
    val military = varchar("military", 100).nullable()
    val studentCardNumber = varchar("student_card_number", 100).nullable()
    val studyType = varchar("study_type", 100).nullable()
    val studyForm = varchar("study_form", 100).nullable()
    val status = varchar("status", 100).nullable()
    val fatherFio = varchar("father_fio", 255).nullable()
    val fatherPhone = varchar("father_phone", 50).nullable()
    val fatherAddress = text("father_address").nullable()
    val motherFio = varchar("mother_fio", 255).nullable()
    val motherPhone = varchar("mother_phone", 50).nullable()
    val motherAddress = text("mother_address").nullable()
}