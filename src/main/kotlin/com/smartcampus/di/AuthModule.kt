package com.smartcampus.di

import com.smartcampus.core.database.auth.entities.AccessGrantsTable
import com.smartcampus.core.database.auth.entities.DepartmentsTable
import com.smartcampus.core.database.auth.entities.PermissionsTable
import com.smartcampus.core.database.auth.entities.SuperusersTable
import com.smartcampus.core.database.auth.entities.UsersTable
import com.smartcampus.features.auth.AuthService
import com.smartcampus.features.auth.AuthRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val authModule = module {
    singleOf(::AuthService)
    singleOf(::AuthRepository)
    single { AccessGrantsTable }
    single { DepartmentsTable }
    single { PermissionsTable }
    single { SuperusersTable }
    single { UsersTable }
}