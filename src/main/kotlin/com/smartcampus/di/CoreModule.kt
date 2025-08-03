package com.smartcampus.di

import com.smartcampus.data.database.auth.SmartCampusAuthDb
import com.smartcampus.data.database.base.DataSourceFactory
import com.smartcampus.data.database.smartCampus.SmartCampusDb
import com.smartcampus.domain.security.PasswordHasher
import com.smartcampus.domain.security.TokenUtils
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val coreModule = module {
    singleOf(::TokenUtils)
    single { PasswordHasher }
    singleOf(::DataSourceFactory)
    singleOf(::SmartCampusAuthDb)
    singleOf(::SmartCampusDb)
}