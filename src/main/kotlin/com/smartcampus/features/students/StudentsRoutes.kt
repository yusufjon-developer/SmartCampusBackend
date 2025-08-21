package com.smartcampus.features.students

import com.smartcampus.app.utils.deleteWithAccess
import com.smartcampus.app.utils.getWithAccess
import com.smartcampus.app.utils.putWithAccess
import com.smartcampus.domain.models.StudentUpdateRequest
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.security.models.Permissions
import com.smartcampus.domain.security.models.UserSessionPrincipal
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.route

fun Route.studentsRoutes(service: StudentsService) {

    route("/students") {
        getWithAccess(null, Permissions.STUDENTS_READ_ALL) {
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 20
            val search = call.request.queryParameters["search"]
            val params = PageRequestParams(page, pageSize, search)
            val result = service.getStudents(params)
            call.respond(result)
        }

        getWithAccess("{id}", Permissions.STUDENTS_READ_ALL, Permissions.STUDENTS_READ_OWN) {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid student id")
                return@getWithAccess
            }
            val student = service.getStudentById(id)
            if (student != null) call.respond(student) else call.respond(HttpStatusCode.NotFound, "Student not found")
        }

        putWithAccess("{id}", Permissions.STUDENTS_UPDATE_ALL, Permissions.STUDENTS_UPDATE_OWN) {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid student id"); return@putWithAccess
            }
            val request = call.receive<StudentUpdateRequest>()
            val updated = service.updateStudent(id, request, performingUserId = call.principal<UserSessionPrincipal>()?.userId ?: 1)
            if (updated != null) call.respond(updated) else call.respond(HttpStatusCode.NotFound, "Student not found")
        }

        deleteWithAccess("{id}", Permissions.STUDENTS_DELETE) {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) { call.respond(HttpStatusCode.BadRequest, "Invalid student id"); return@deleteWithAccess }
            val deleted = service.deleteStudent(id)
            if (deleted) call.respond("Student deleted") else call.respond(HttpStatusCode.NotFound, "Student not found")
        }
    }


}

