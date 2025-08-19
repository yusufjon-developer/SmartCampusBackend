package com.smartcampus.data.database.smartCampus.dao

import com.smartcampus.data.database.smartCampus.SmartCampusDb
import com.smartcampus.data.database.smartCampus.entities.GroupsTable
import com.smartcampus.data.database.smartCampus.entities.StudentsInfoTable
import com.smartcampus.data.database.smartCampus.entities.StudentsTable
import com.smartcampus.data.database.smartCampus.entities.TeachersInfoTable
import com.smartcampus.data.database.smartCampus.entities.TeachersTable
import com.smartcampus.domain.models.auth.StudentCreateRequest
import com.smartcampus.domain.models.auth.StudentInfoCreateRequest
import com.smartcampus.domain.models.auth.TeacherCreateRequest
import com.smartcampus.domain.models.auth.TeacherInfoCreateRequest
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.slf4j.LoggerFactory
import java.time.LocalDate

class SmartCampusProfileDao(private val db: SmartCampusDb) {
    private val log = LoggerFactory.getLogger(SmartCampusProfileDao::class.java)

    suspend fun createStudentWithOptionalInfo(
        student: StudentCreateRequest,
        info: StudentInfoCreateRequest?
    ): Int =
        db.query {
            val dob: LocalDate? =
                student.birthday?.takeIf { it.isNotBlank() }?.let { LocalDate.parse(it) }
            val insertedId = StudentsTable.insertAndGetId { row ->
                row[StudentsTable.surname] = student.surname
                row[StudentsTable.name] = student.name
                row[StudentsTable.lastname] = student.lastname
                row[StudentsTable.birthday] = dob
                // StudentsTable.groupId is reference to GroupsTable in same DB -> use EntityID
                row[StudentsTable.groupId] = student.groupId?.let { EntityID(it, GroupsTable) }
                row[StudentsTable.phoneNumber] = student.phoneNumber
            }
            val sid = insertedId.value
            if (info != null) {
                StudentsInfoTable.insert { r ->
                    r[StudentsInfoTable.studentId] = sid
                    r[StudentsInfoTable.address] = info.address
                    r[StudentsInfoTable.passportNumber] = info.passportNumber
                    r[StudentsInfoTable.school] = info.school
                    r[StudentsInfoTable.documentNumber] = info.documentNumber
                    r[StudentsInfoTable.military] = info.military
                    r[StudentsInfoTable.studentCardNumber] = info.studentCardNumber
                    r[StudentsInfoTable.studyType] = info.studyType
                    r[StudentsInfoTable.studyForm] = info.studyForm
                    r[StudentsInfoTable.status] = info.status
                    r[StudentsInfoTable.fatherFio] = info.fatherFio
                    r[StudentsInfoTable.fatherPhone] = info.fatherPhone
                    r[StudentsInfoTable.fatherAddress] = info.fatherAddress
                    r[StudentsInfoTable.motherFio] = info.motherFio
                    r[StudentsInfoTable.motherPhone] = info.motherPhone
                    r[StudentsInfoTable.motherAddress] = info.motherAddress
                }
            }
            sid
        }

    suspend fun createTeacherWithOptionalInfo(
        teacher: TeacherCreateRequest,
        info: TeacherInfoCreateRequest?
    ): Int =
        db.query {
            val dob: LocalDate? =
                teacher.birthday?.takeIf { it.isNotBlank() }?.let { LocalDate.parse(it) }
            val insertedId = TeachersTable.insertAndGetId { row ->
                row[TeachersTable.surname] = teacher.surname
                row[TeachersTable.name] = teacher.name
                row[TeachersTable.lastname] = teacher.lastname
                row[TeachersTable.birthday] = dob
                row[TeachersTable.phoneNumber] = teacher.phoneNumber
            }
            val tid = insertedId.value
            if (info != null) {
                TeachersInfoTable.insert { r ->
                    r[TeachersInfoTable.teacherId] = tid
                    r[TeachersInfoTable.address] = info.address
                    r[TeachersInfoTable.passportNumber] = info.passportNumber
                    r[TeachersInfoTable.highSchool] = info.highSchool
                    r[TeachersInfoTable.documentNumber] = info.documentNumber
                    r[TeachersInfoTable.military] = info.military
                    r[TeachersInfoTable.degree] = info.degree
                    r[TeachersInfoTable.title] = info.title
                    r[TeachersInfoTable.position] = info.position
                }
            }
            tid
        }

    suspend fun deleteStudentCascade(studentId: Int) = db.query {
        StudentsInfoTable.deleteWhere { StudentsInfoTable.studentId eq studentId }
        StudentsTable.deleteWhere { StudentsTable.id eq studentId }
    }

    suspend fun deleteTeacherCascade(teacherId: Int) = db.query {
        TeachersInfoTable.deleteWhere { TeachersInfoTable.teacherId eq teacherId }
        TeachersTable.deleteWhere { TeachersTable.id eq teacherId }
    }
}
