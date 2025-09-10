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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun Route.auditoriumsRoutes(service: AuditoriumsService) {
    route("/auditoriums") {
        getWithAccess(null, Permissions.AUDITORIUMS_READ) {
            val params = call.getPageRequestParams()
            val isAvailable = call.request.queryParameters["available"]?.toBooleanStrictOrNull() ?: false
            val date = call.request.queryParameters["day"] ?: LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val result = service.listAuditoriums(params, isAvailable, date)
            call.respond(HttpStatusCode.OK, result)
        }

        getWithAccess("{id}", Permissions.AUDITORIUMS_READ) {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: run { call.respond(HttpStatusCode.BadRequest); return@getWithAccess }
            try {
                val item = service.getAuditoriumById(id)
                call.respond(HttpStatusCode.OK, item)
            } catch (_: NoSuchElementException) {
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
            } catch (_: NoSuchElementException) {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        deleteWithAccess("{id}", Permissions.AUDITORIUMS_DELETE) {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: run { call.respond(HttpStatusCode.BadRequest); return@deleteWithAccess }
            try {
                service.deleteAuditorium(id)
                call.respond(HttpStatusCode.NoContent)
            } catch (_: NoSuchElementException) {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}