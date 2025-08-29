package com.smartcampus.features.curriculums

import com.smartcampus.app.utils.deleteWithAccess
import com.smartcampus.app.utils.getWithAccess
import com.smartcampus.app.utils.postWithAccess
import com.smartcampus.app.utils.putWithAccess
import com.smartcampus.domain.models.CurriculumCreateRequest
import com.smartcampus.domain.models.CurriculumDisciplineCreateRequest
import com.smartcampus.domain.models.CurriculumDisciplineUpdateRequest
import com.smartcampus.domain.models.CurriculumUpdateRequest
import com.smartcampus.domain.security.models.Permissions
import com.smartcampus.features.common.getPageRequestParams
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.curriculumsRoutes(service: CurriculumsService) {
    route("/crm/curriculums") {
        getWithAccess(null, Permissions.CURRICULUMS_READ) {
            val params = call.getPageRequestParams()
            val result = service.listCurriculums(params)
            call.respond(HttpStatusCode.OK, result)
        }

        getWithAccess("{id}", Permissions.CURRICULUMS_READ) {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid id"); return@getWithAccess
            }
            val curr = service.getCurriculum(id)
            if (curr == null) call.respond(HttpStatusCode.NotFound, "Curriculum not found")
            else call.respond(curr)
        }

        postWithAccess(null, Permissions.CURRICULUMS_CREATE) {
            val req = call.receive<CurriculumCreateRequest>()
            val created = service.createCurriculum(req)
            call.respond(HttpStatusCode.Created, created)
        }

        putWithAccess("{id}", Permissions.CURRICULUMS_UPDATE) {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid id"); return@putWithAccess
            }
            val req = call.receive<CurriculumUpdateRequest>()
            val updated = service.updateCurriculum(id, req)
            if (updated == null) call.respond(HttpStatusCode.NotFound, "Curriculum not found")
            else call.respond(updated)
        }

        deleteWithAccess("{id}", Permissions.CURRICULUMS_DELETE) {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid id"); return@deleteWithAccess
            }
            val deleted = service.deleteCurriculum(id)
            if (deleted) call.respond(HttpStatusCode.NoContent)
            else call.respond(HttpStatusCode.NotFound, "Curriculum not found")
        }

        // disciplines endpoints
        postWithAccess("{id}/disciplines", Permissions.CURRICULUM_DISCIPLINES_MANAGE) {
            val curriculumId = call.parameters["id"]?.toIntOrNull()
                ?: run { call.respond(HttpStatusCode.BadRequest); return@postWithAccess }
            val req = call.receive<CurriculumDisciplineCreateRequest>()
            val added = service.addDiscipline(curriculumId, req)
            if (added == null) call.respond(HttpStatusCode.BadRequest, "Could not add discipline")
            else call.respond(HttpStatusCode.Created, added)
        }

        putWithAccess("disciplines/{cdId}", Permissions.CURRICULUM_DISCIPLINES_MANAGE) {
            val cdId = call.parameters["cdId"]?.toIntOrNull()
                ?: run { call.respond(HttpStatusCode.BadRequest); return@putWithAccess }
            val req = call.receive<CurriculumDisciplineUpdateRequest>()
            val updated = service.updateDiscipline(cdId, req)
            if (updated == null) call.respond(HttpStatusCode.NotFound, "Curriculum discipline not found")
            else call.respond(HttpStatusCode.OK, updated)
        }

        deleteWithAccess("disciplines/{cdId}", Permissions.CURRICULUM_DISCIPLINES_MANAGE) {
            val cdId = call.parameters["cdId"]?.toIntOrNull()
                ?: run { call.respond(HttpStatusCode.BadRequest); return@deleteWithAccess }
            val deleted = service.deleteDiscipline(cdId)
            if (deleted) call.respond(HttpStatusCode.NoContent)
            else call.respond(HttpStatusCode.NotFound, "Curriculum discipline not found")
        }
    }
}
