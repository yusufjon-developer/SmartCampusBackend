package com.smartcampus.di

import com.smartcampus.core.database.auth.SmartCampusAuthDb
import com.smartcampus.core.database.base.DataSourceFactory
import com.smartcampus.core.database.smartCampus.SmartCampusDb
import com.smartcampus.core.security.PasswordHasher
import com.smartcampus.core.security.TokenUtils
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val coreModule = module {
    singleOf(::TokenUtils)
    single { PasswordHasher }
    single { DataSourceFactory }
    single { SmartCampusAuthDb }
    single { SmartCampusDb }

}