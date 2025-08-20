package com.smartcampus.di.featuresModule

import com.smartcampus.data.dao.SystemAdminDao
import com.smartcampus.data.repositories.SystemAdminRepositoryImpl
import com.smartcampus.domain.repositories.SystemAdminRepository
import com.smartcampus.features.systemAdmin.SystemAdminService
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val systemAdminModule = module {
    singleOf(::SystemAdminDao)
    singleOf(::SystemAdminRepositoryImpl) bind SystemAdminRepository::class
    singleOf(::SystemAdminService)
}