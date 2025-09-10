package com.smartcampus.features.disciplines

import com.smartcampus.app.utils.deleteWithAccess
import com.smartcampus.app.utils.getWithAccess
import com.smartcampus.app.utils.postWithAccess
import com.smartcampus.app.utils.putWithAccess
import com.smartcampus.domain.models.DisciplineCreateRequest
import com.smartcampus.domain.models.DisciplineUpdateRequest
import com.smartcampus.domain.security.models.Permissions
import com.smartcampus.features.common.getPageRequestParams
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.disciplinesRoutes(service: DisciplinesService) {
    route("/crm/disciplines") {
        getWithAccess(null, Permissions.DISCIPLINES_READ) {
            val params = call.getPageRequestParams()
            val result = service.listDisciplines(params)
            call.respond(HttpStatusCode.OK, result)
        }

        getWithAccess("{id}", Permissions.DISCIPLINES_READ) {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) { call.respond(HttpStatusCode.BadRequest, "Invalid id"); return@getWithAccess }
            val item = service.getDiscipline(id)
            if (item == null) call.respond(HttpStatusCode.NotFound, "Discipline not found") else call.respond(HttpStatusCode.OK, item)
        }

        postWithAccess(null, Permissions.DISCIPLINES_CREATE) {
            val req = call.receive<DisciplineCreateRequest>()
            val created = service.createDiscipline(req)
            call.respond(HttpStatusCode.Created, created)
        }

        putWithAccess("{id}", Permissions.DISCIPLINES_UPDATE) {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) { call.respond(HttpStatusCode.BadRequest, "Invalid id"); return@putWithAccess }
            val req = call.receive<DisciplineUpdateRequest>()
            val updated = service.updateDiscipline(id, req)
            if (updated == null) call.respond(HttpStatusCode.NotFound, "Discipline not found") else call.respond(HttpStatusCode.OK, updated)
        }

        deleteWithAccess("{id}", Permissions.DISCIPLINES_DELETE) {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) { call.respond(HttpStatusCode.BadRequest, "Invalid id"); return@deleteWithAccess }
            val deleted = service.deleteDiscipline(id)
            if (deleted) call.respond(HttpStatusCode.NoContent) else call.respond(HttpStatusCode.NotFound, "Discipline not found")
        }
    }
}
