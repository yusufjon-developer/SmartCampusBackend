package com.smartcampus.features.auditoriums

import com.smartcampus.app.utils.deleteWithAccess
import com.smartcampus.app.utils.getWithAccess
import com.smartcampus.app.utils.postWithAccess
import com.smartcampus.app.utils.putWithAccess
import com.smartcampus.domain.models.AuditoriumCreateRequest
import com.smartcampus.domain.models.AuditoriumUpdateRequest
import com.smartcampus.domain.security.models.Permissions
import com.smartcampus.features.common.getPageRequestParams
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.auditoriumsRoutes(service: AuditoriumsService) {
    route("/auditoriums") {
        getWithAccess(null, Permissions.AUDITORIUMS_READ) {
            val params = call.getPageRequestParams()
            val result = service.listAuditoriums(params)
            call.respond(HttpStatusCode.OK, result)
        }

        getWithAccess("{id}", Permissions.AUDITORIUMS_READ) {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: run { call.respond(HttpStatusCode.BadRequest); return@getWithAccess }
            try {
                val item = service.getAuditoriumById(id)
                call.respond(HttpStatusCode.OK, item)
            } catch (e: NoSuchElementException) {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        postWithAccess(null, Permissions.AUDITORIUMS_CREATE) {
            val req = call.receive<AuditoriumCreateRequest>()
            try {
                val created = service.createAuditorium(req)
                call.respond(HttpStatusCode.Created, created)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request data")
            }
        }

        putWithAccess("{id}", Permissions.AUDITORIUMS_UPDATE) {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: run { call.respond(HttpStatusCode.BadRequest); return@putWithAccess }
            val req = call.receive<AuditoriumUpdateRequest>()
            try {
                val updated = service.updateAuditorium(id, req)
                call.respond(HttpStatusCode.OK, updated)
            } catch (e: NoSuchElementException) {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        deleteWithAccess("{id}", Permissions.AUDITORIUMS_DELETE) {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: run { call.respond(HttpStatusCode.BadRequest); return@deleteWithAccess }
            try {
                service.deleteAuditorium(id)
                call.respond(HttpStatusCode.NoContent)
            } catch (e: NoSuchElementException) {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}