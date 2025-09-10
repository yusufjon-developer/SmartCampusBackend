package com.smartcampus.features.workload

import com.smartcampus.app.utils.deleteWithAccess
import com.smartcampus.app.utils.getWithAccess
import com.smartcampus.app.utils.postWithAccess
import com.smartcampus.app.utils.putWithAccess
import com.smartcampus.domain.models.ExecutedHoursResponse
import com.smartcampus.domain.models.TeacherWorkloadCreateRequest
import com.smartcampus.domain.models.TeacherWorkloadUpdateRequest
import com.smartcampus.domain.models.WorkloadExecutionCreateRequest
import com.smartcampus.domain.security.models.Permissions
import com.smartcampus.features.common.getPageRequestParams
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.workloadRoutes(service: WorkloadService) {
    route("/crm/workloads") {
        getWithAccess(null, Permissions.TEACHERS_WORKLOAD_READ_ALL) {
            val params = call.getPageRequestParams()
            val result = service.listWorkloads(params)
            call.respond(HttpStatusCode.OK, result)
        }

        getWithAccess("{id}", Permissions.TEACHERS_WORKLOAD_READ_ALL, Permissions.TEACHERS_WORKLOAD_READ_OWN) {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@getWithAccess call.respond(HttpStatusCode.BadRequest, "Invalid id")
            val item = service.getWorkload(id)
            if (item == null) call.respond(HttpStatusCode.NotFound, "Workload not found")
            else call.respond(HttpStatusCode.OK, item)
        }

        postWithAccess(null, Permissions.TEACHERS_WORKLOAD_CREATE) {
            val req = call.receive<TeacherWorkloadCreateRequest>()
            when (val res = service.createWorkload(req)) {
                is WorkloadService.EitherErrorOrDto.Error -> call.respond(HttpStatusCode.BadRequest, res.message)
                is WorkloadService.EitherErrorOrDto.Ok -> call.respond(HttpStatusCode.Created, res.value)
            }
        }

        putWithAccess("{id}", Permissions.TEACHERS_WORKLOAD_UPDATE) {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@putWithAccess call.respond(HttpStatusCode.BadRequest, "Invalid id")
            val req = call.receive<TeacherWorkloadUpdateRequest>()
            try {
                val updated = service.updateWorkload(id, req)
                if (updated == null) call.respond(HttpStatusCode.NotFound, "Workload not found")
                else call.respond(HttpStatusCode.OK, updated)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid data")
            }
        }

        deleteWithAccess("{id}", Permissions.TEACHERS_WORKLOAD_DELETE) {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@deleteWithAccess call.respond(HttpStatusCode.BadRequest, "Invalid id")
            val deleted = service.deleteWorkload(id)
            if (deleted) call.respond(HttpStatusCode.NoContent)
            else call.respond(HttpStatusCode.NotFound, "Workload not found")
        }

        // ---------------- executions ----------------
        getWithAccess("{id}/executions", Permissions.TEACHERS_WORKLOAD_READ_ALL, Permissions.TEACHERS_WORKLOAD_READ_OWN) {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@getWithAccess call.respond(HttpStatusCode.BadRequest, "Invalid id")
            val list = service.listExecutions(id)
            call.respond(HttpStatusCode.OK, list)
        }

        postWithAccess("{id}/executions", Permissions.TEACHERS_WORKLOAD_UPDATE) {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@postWithAccess call.respond(HttpStatusCode.BadRequest, "Invalid id")
            val req = call.receive<WorkloadExecutionCreateRequest>()
            try {
                val created = service.addExecution(id, req)
                if (created == null) call.respond(HttpStatusCode.BadRequest, "Workload not found")
                else call.respond(HttpStatusCode.Created, created)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid data")
            }
        }

        getWithAccess("{id}/executions/sum", Permissions.TEACHERS_WORKLOAD_READ_ALL, Permissions.TEACHERS_WORKLOAD_READ_OWN) {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@getWithAccess call.respond(HttpStatusCode.BadRequest, "Invalid id")
            val sum = service.sumExecutedHours(id)
            call.respond(HttpStatusCode.OK, ExecutedHoursResponse(id, sum))
        }

        // ---------------- teacher workloads ----------------
        getWithAccess("teacher/{teacherId}", Permissions.TEACHERS_WORKLOAD_READ_ALL, Permissions.TEACHERS_WORKLOAD_READ_OWN) {
            val teacherId = call.parameters["teacherId"]?.toIntOrNull()
                ?: return@getWithAccess call.respond(HttpStatusCode.BadRequest, "Invalid teacherId")

            val academicYear = call.request.queryParameters["academicYear"]

            val list = service.listForTeacher(teacherId, academicYear)
            call.respond(HttpStatusCode.OK, list)
        }
    }
}
