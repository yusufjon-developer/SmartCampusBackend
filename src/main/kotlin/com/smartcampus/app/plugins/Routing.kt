package com.smartcampus.app.plugins

import com.smartcampus.domain.security.AccessControlService
import com.smartcampus.domain.security.models.UserSessionPrincipal
import com.smartcampus.features.auth.AuthService
import com.smartcampus.features.auth.authRoutes
import com.smartcampus.features.curriculums.CurriculumsService
import com.smartcampus.features.curriculums.curriculumsRoutes
import com.smartcampus.features.disciplines.DisciplinesService
import com.smartcampus.features.disciplines.disciplinesRoutes
import com.smartcampus.features.groups.GroupsService
import com.smartcampus.features.groups.groupsRoutes
import com.smartcampus.features.specialities.SpecialitiesService
import com.smartcampus.features.specialities.specialitiesRoutes
import com.smartcampus.features.students.StudentsService
import com.smartcampus.features.students.studentsRoutes
import com.smartcampus.features.subjects.SubjectsService
import com.smartcampus.features.subjects.subjectsRoutes
import com.smartcampus.features.systemAdmin.SystemAdminService
import com.smartcampus.features.systemAdmin.systemAdminRoutes
import com.smartcampus.features.teachers.TeachersService
import com.smartcampus.features.teachers.teachersRoutes
import com.smartcampus.features.workload.WorkloadService
import com.smartcampus.features.workload.workloadRoutes
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import org.koin.ktor.ext.inject

private val AccessControlKey = AttributeKey<AccessControlService>("AccessControlService")

val Application.accessControlService: AccessControlService
    get() = this.attributes.getOrNull(AccessControlKey)
        ?: error("AccessControlService is not registered in Application attributes")

fun Application.configureRouting() {

    val accessControlService by inject<AccessControlService>()
    val authService by inject<AuthService>()
    val curriculumsService by inject<CurriculumsService>()
    val systemAdminService by inject<SystemAdminService>()
    val studentsService by inject<StudentsService>()
    val teachersService by inject<TeachersService>()
    val groupsService by inject<GroupsService>()
    val specialitiesService by inject<SpecialitiesService>()
    val disciplinesService by inject<DisciplinesService>()
    val subjectsService by inject<SubjectsService>()
    val workloadService by inject<WorkloadService>()

    attributes.put(AccessControlKey, accessControlService)

    routing {
        authRoutes(authService)
        specialitiesRoutes(specialitiesService)

        // в configureRouting / routing { authenticate("auth-jwt") { ... } }
        authenticate("auth-jwt") {
            route("/debug") {
                get("/me/permissions") {
                    val principal = call.principal<UserSessionPrincipal>()
                    if (principal == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "no principal"))
                        return@get
                    }
                    val systemAdminDao by inject<com.smartcampus.data.dao.SystemAdminDao>()
                    val rolePerms = principal.roleId?.let { systemAdminDao.getPermissionIdsForRole(it) } ?: emptySet()
                    val indivPerms = systemAdminDao.getIndividualPermissionIdsForUser(principal.userId)
                    val all = rolePerms + indivPerms
                    val names = systemAdminDao.getPermissionNamesByIds(all)
                    call.respond(
                        mapOf(
                            "principal" to mapOf(
                                "userId" to principal.userId,
                                "username" to principal.username,
                                "roleNames" to principal.roleNames,
                                "roleId" to principal.roleId
                            ),
                            "rolePermissionIds" to rolePerms,
                            "individualPermissionIds" to indivPerms,
                            "allPermissionIds" to all,
                            "permissionNames" to names
                        )
                    )
                }
            }

            // ваши реальные маршруты
            studentsRoutes(studentsService)
            teachersRoutes(teachersService)
            curriculumsRoutes(curriculumsService)
            groupsRoutes(groupsService)
            disciplinesRoutes(disciplinesService)
            subjectsRoutes(subjectsService)
            workloadRoutes(workloadService)
        }


        authenticate("auth-jwt-admin") {
            systemAdminRoutes(systemAdminService)
        }

        log.info("Routing configured.")
    }
}