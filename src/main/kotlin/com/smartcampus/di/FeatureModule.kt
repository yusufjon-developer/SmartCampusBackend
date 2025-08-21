package com.smartcampus.di

import com.smartcampus.features.auditoriums.AuditoriumsService
import com.smartcampus.features.auth.AuthService
import com.smartcampus.features.curriculums.CurriculumsService
import com.smartcampus.features.students.StudentsService
import com.smartcampus.features.systemAdmin.SystemAdminService
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val featureModule = module {
    singleOf(::AuditoriumsService)
    singleOf(::AuthService)
    singleOf(::CurriculumsService)
    singleOf(::StudentsService)
    singleOf(::SystemAdminService)
}