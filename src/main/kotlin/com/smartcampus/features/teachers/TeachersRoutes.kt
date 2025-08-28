package com.smartcampus.features.teachers

import com.smartcampus.app.utils.deleteWithAccess
import com.smartcampus.app.utils.getWithAccess
import com.smartcampus.app.utils.putWithAccess
import com.smartcampus.domain.models.TeacherUpdateRequest
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.security.models.Permissions
import com.smartcampus.domain.security.models.UserSessionPrincipal
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.teachersRoutes(service: TeachersService) {
    route("/teachers") {
        getWithAccess(null, Permissions.TEACHERS_READ_ALL) {
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 20
            val search = call.request.queryParameters["search"]
            val params = PageRequestParams(page, pageSize, search)
            val result = service.getTeachers(params)
            call.respond(result)
        }

        getWithAccess("{id}", Permissions.TEACHERS_READ_ALL) {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) { call.respond(HttpStatusCode.BadRequest, "Invalid teacher id"); return@getWithAccess }
            val teacher = service.getTeacherById(id)
            if (teacher != null) call.respond(teacher) else call.respond(HttpStatusCode.NotFound, "Teacher not found")
        }

        getWithAccess("{id}/info", Permissions.TEACHERS_READ_INFO) {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) { call.respond(HttpStatusCode.BadRequest, "Invalid teacher id"); return@getWithAccess }
            val sensitive = service.getTeacherSensitiveInfo(id)
            if (sensitive != null) call.respond(sensitive) else call.respond(HttpStatusCode.NotFound, "Teacher sensitive info not found")
        }

        putWithAccess("{id}", Permissions.TEACHERS_UPDATE_ALL, Permissions.TEACHERS_UPDATE_OWN) {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) { call.respond(HttpStatusCode.BadRequest, "Invalid teacher id"); return@putWithAccess }
            val request = call.receive<TeacherUpdateRequest>()
            val updated = service.updateTeacher(id, request, performingUserId = call.principal<UserSessionPrincipal>()?.userId ?: 1)
            if (updated != null) call.respond(updated) else call.respond(HttpStatusCode.NotFound, "Teacher not found")
        }

        deleteWithAccess("{id}", Permissions.TEACHERS_DELETE) {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) { call.respond(HttpStatusCode.BadRequest, "Invalid teacher id"); return@deleteWithAccess }
            val deleted = service.deleteTeacher(id)
            if (deleted) call.respond("Teacher deleted") else call.respond(HttpStatusCode.NotFound, "Teacher not found")
        }
    }
}
