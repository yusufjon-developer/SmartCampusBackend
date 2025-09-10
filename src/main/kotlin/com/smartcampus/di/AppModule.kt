package com.smartcampus.di

import com.smartcampus.domain.security.AccessControlService
import com.smartcampus.domain.security.models.JwtConfig
import io.ktor.server.application.*
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

fun appModule(environment: ApplicationEnvironment) = module {
    single {
        val jwtConfigPath = "jwt"
        JwtConfig(
            secret = environment.config.property("${jwtConfigPath}.secret").getString(),
            issuer = environment.config.property("${jwtConfigPath}.issuer").getString(),
            audience = environment.config.property("${jwtConfigPath}.audience").getString(),
            realm = environment.config.property("${jwtConfigPath}.realm").getString(),
            validityInMs = environment.config.propertyOrNull("${jwtConfigPath}.validityInMs")?.getString()?.toLong()
            ?: (3_600_000 * 24)
        )
    }

    singleOf(::AccessControlService)
}