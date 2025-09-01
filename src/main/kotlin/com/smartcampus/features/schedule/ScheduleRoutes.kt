package com.smartcampus.features.schedule

import com.smartcampus.app.utils.deleteWithAccess
import com.smartcampus.app.utils.getWithAccess
import com.smartcampus.app.utils.postWithAccess
import com.smartcampus.app.utils.putWithAccess
import com.smartcampus.domain.models.ScheduleCreateRequest
import com.smartcampus.domain.models.ScheduleUpdateRequest
import com.smartcampus.domain.security.models.Permissions
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.scheduleRoutes(service: ScheduleService) {
    route("/crm/schedule") {
        // list with optional filters
        get {
            val day = call.request.queryParameters["day"]
            val teacherId = call.request.queryParameters["teacherId"]?.toIntOrNull()
            val groupId = call.request.queryParameters["groupId"]?.toIntOrNull()
            val auditoriumId = call.request.queryParameters["auditoriumId"]?.toIntOrNull()
            val res = service.listSchedules(day, teacherId, groupId, auditoriumId)
            call.respond(HttpStatusCode.OK, res)
        }

        authenticate("auth-jwt") {
            getWithAccess("{id}", Permissions.SCHEDULE_READ_ALL) {
                val id = call.parameters["id"]?.toIntOrNull() ?: run {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        "Invalid id"
                    ); return@getWithAccess
                }
                val item = service.getSchedule(id)
                if (item == null) call.respond(HttpStatusCode.NotFound, "Schedule not found") else call.respond(
                    HttpStatusCode.OK,
                    item
                )
            }

            postWithAccess(null, Permissions.SCHEDULE_CREATE) {
                val req = call.receive<ScheduleCreateRequest>()
                try {
                    val created = service.createSchedule(req)
                    call.respond(HttpStatusCode.Created, created)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                } catch (e: IllegalStateException) {
                    call.respond(HttpStatusCode.Conflict, mapOf("error" to e.message))
                }
            }

            putWithAccess("{id}", Permissions.SCHEDULE_UPDATE) {
                val id = call.parameters["id"]?.toIntOrNull() ?: run {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        "Invalid id"
                    ); return@putWithAccess
                }
                val req = call.receive<ScheduleUpdateRequest>()
                try {
                    val updated = service.updateSchedule(id, req)
                    call.respond(HttpStatusCode.OK, updated)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                } catch (e: IllegalStateException) {
                    call.respond(HttpStatusCode.Conflict, mapOf("error" to e.message))
                }
            }

            deleteWithAccess("{id}", Permissions.SCHEDULE_DELETE) {
                val id = call.parameters["id"]?.toIntOrNull() ?: run {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        "Invalid id"
                    ); return@deleteWithAccess
                }
                val deleted = service.deleteSchedule(id)
                if (deleted) call.respond(HttpStatusCode.NoContent) else call.respond(
                    HttpStatusCode.NotFound,
                    "Schedule not found"
                )
            }

            // validate without creating
            postWithAccess("validate", Permissions.SCHEDULE_CREATE) {
                val req = call.receive<ScheduleCreateRequest>()
                val conflicts = service.validateRequest(req)
                if (conflicts.isEmpty()) {
                    call.respond(HttpStatusCode.OK, emptyList<Any>())
                } else {
                    call.respond(HttpStatusCode.Conflict, conflicts)
                }
            }
        }
    }
}
