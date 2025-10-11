package com.smartcampus.features.groups

import com.smartcampus.app.utils.deleteWithAccess
import com.smartcampus.app.utils.getWithAccess
import com.smartcampus.app.utils.postWithAccess
import com.smartcampus.app.utils.putWithAccess
import com.smartcampus.domain.models.GroupCreateRequest
import com.smartcampus.domain.models.GroupUpdateRequest
import com.smartcampus.domain.security.models.Permissions
import com.smartcampus.features.common.getPageRequestParams
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.groupsRoutes(service: GroupsService) {
    route("/groups") {
        getWithAccess(null, Permissions.GROUPS_READ) {
            val params = call.getPageRequestParams()
            val result = service.listGroups(params)
            call.respond(HttpStatusCode.OK, result)
        }

        getWithAccess("{id}", Permissions.GROUPS_READ) {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: run { call.respond(HttpStatusCode.BadRequest); return@getWithAccess }
            val item = service.getGroup(id)
            if (item == null) call.respond(HttpStatusCode.NotFound)
            else call.respond(HttpStatusCode.OK, item)
        }

        getWithAccess("{id}/students", Permissions.GROUPS_READ) {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: run { call.respond(HttpStatusCode.BadRequest); return@getWithAccess }
            val result = service.listStudentsInGroup(id)
            if (result == null) call.respond(HttpStatusCode.NotFound)
            else call.respond(HttpStatusCode.OK, result)
        }

        postWithAccess(null, Permissions.GROUPS_CREATE) {
            val req = call.receive<GroupCreateRequest>()
            val created = service.createGroup(req)
            call.respond(HttpStatusCode.Created, created)
        }

        putWithAccess("{id}", Permissions.GROUPS_UPDATE) {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: run { call.respond(HttpStatusCode.BadRequest); return@putWithAccess }
            val req = call.receive<GroupUpdateRequest>()
            val updated = service.updateGroup(id, req)
            if (updated == null) call.respond(HttpStatusCode.NotFound)
            else call.respond(HttpStatusCode.OK, updated)
        }

        deleteWithAccess("{id}", Permissions.GROUPS_DELETE) {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: run { call.respond(HttpStatusCode.BadRequest); return@deleteWithAccess }
            val deleted = service.deleteGroup(id)
            if (deleted) call.respond(HttpStatusCode.NoContent)
            else call.respond(HttpStatusCode.NotFound)
        }
    }
}
