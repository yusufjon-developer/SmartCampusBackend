package com.smartcampus.di.featuresModule

import com.smartcampus.data.dao.AuthDao
import com.smartcampus.data.dao.SmartCampusProfileDao
import com.smartcampus.data.repositories.AuthRepositoryImpl
import com.smartcampus.domain.repositories.AuthRepository
import com.smartcampus.features.auth.AuthService
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module


val authModule = module {
    singleOf(::SmartCampusProfileDao)
    singleOf(::AuthDao)
    singleOf(::AuthRepositoryImpl) bind AuthRepository::class
    singleOf(::AuthService)
}