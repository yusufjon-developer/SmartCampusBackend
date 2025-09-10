package com.smartcampus.features.specialities

import com.smartcampus.app.utils.deleteWithAccess
import com.smartcampus.app.utils.postWithAccess
import com.smartcampus.app.utils.putWithAccess
import com.smartcampus.domain.models.SpecialityCreateRequest
import com.smartcampus.domain.models.SpecialityUpdateRequest
import com.smartcampus.domain.security.models.Permissions
import com.smartcampus.features.common.getPageRequestParams
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.specialitiesRoutes(service: SpecialitiesService) {
    route("/specialities") {
        get {
            val params = call.getPageRequestParams()
            val result = service.listSpecialities(params)
            call.respond(HttpStatusCode.OK, result)
        }

        get("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: run { call.respond(HttpStatusCode.BadRequest); return@get }
            val item = service.getSpeciality(id)
            if (item == null) call.respond(HttpStatusCode.NotFound)
            else call.respond(HttpStatusCode.OK, item)
        }
        authenticate("auth-jwt") {
            postWithAccess(null, Permissions.SPECIALITIES_CREATE) {
                val req = call.receive<SpecialityCreateRequest>()
                val created = service.createSpeciality(req)
                call.respond(HttpStatusCode.Created, created)
            }

            putWithAccess("{id}", Permissions.SPECIALITIES_UPDATE) {
                application.log.info("updated speciality")
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: run { call.respond(HttpStatusCode.BadRequest); return@putWithAccess }
                val req = call.receive<SpecialityUpdateRequest>()
                application.log.info("req: $req")
                val updated = service.updateSpeciality(id, req)
                application.log.info("updated: $updated")
                if (updated == null) call.respond(HttpStatusCode.NotFound)
                else call.respond(HttpStatusCode.OK, updated)
            }

            deleteWithAccess("{id}", Permissions.SPECIALITIES_DELETE) {
                val id = call.parameters["id"]?.toIntOrNull() ?: run { call.respond(HttpStatusCode.BadRequest); return@deleteWithAccess }
                val deleted = service.deleteSpeciality(id)
                if (deleted) call.respond(HttpStatusCode.NoContent)
                else call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}
