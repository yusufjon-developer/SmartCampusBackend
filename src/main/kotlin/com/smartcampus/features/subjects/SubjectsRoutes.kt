package com.smartcampus.features.subjects

import com.smartcampus.app.utils.deleteWithAccess
import com.smartcampus.app.utils.getWithAccess
import com.smartcampus.app.utils.postWithAccess
import com.smartcampus.app.utils.putWithAccess
import com.smartcampus.domain.models.SubjectCreateRequest
import com.smartcampus.domain.models.SubjectUpdateRequest
import com.smartcampus.domain.security.models.Permissions
import com.smartcampus.features.common.getPageRequestParams
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.subjectsRoutes(service: SubjectsService) {
    route("/crm/subjects") {
        // read list
        getWithAccess(null, Permissions.SUBJECTS_READ) {
            val params = call.getPageRequestParams()
            val result = service.listSubjects(params)
            call.respond(HttpStatusCode.OK, result)
        }

        // read single
        getWithAccess("{id}", Permissions.SUBJECTS_READ) {
            val id = call.parameters["id"]?.toIntOrNull() ?: run {
                call.respond(HttpStatusCode.BadRequest, "Invalid id"); return@getWithAccess
            }
            val item = service.getSubject(id)
            if (item == null) call.respond(HttpStatusCode.NotFound, "Subject not found")
            else call.respond(HttpStatusCode.OK, item)
        }


        // create
        postWithAccess(null, Permissions.SUBJECTS_CREATE) {
            val req = call.receive<SubjectCreateRequest>()
            val created = service.createSubject(req)
            call.respond(HttpStatusCode.Created, created)
        }

        // update
        putWithAccess("{id}", Permissions.SUBJECTS_UPDATE) {
            val id = call.parameters["id"]?.toIntOrNull() ?: run {
                call.respond(HttpStatusCode.BadRequest, "Invalid id"); return@putWithAccess
            }
            val req = call.receive<SubjectUpdateRequest>()
            val updated = service.updateSubject(id, req)
            if (updated == null) call.respond(HttpStatusCode.NotFound, "Subject not found")
            else call.respond(HttpStatusCode.OK, updated)
        }

        // delete
        deleteWithAccess("{id}", Permissions.SUBJECTS_DELETE) {
            val id = call.parameters["id"]?.toIntOrNull() ?: run {
                call.respond(HttpStatusCode.BadRequest, "Invalid id"); return@deleteWithAccess
            }
            val deleted = service.deleteSubject(id)
            if (deleted) call.respond(HttpStatusCode.NoContent) else call.respond(
                HttpStatusCode.NotFound,
                "Subject not found"
            )
        }
    }
}
