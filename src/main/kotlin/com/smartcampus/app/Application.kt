package com.smartcampus.app

import com.smartcampus.app.plugins.configureDatabases
import com.smartcampus.app.plugins.configureFrameworks
import com.smartcampus.app.plugins.configureMonitoring
import com.smartcampus.app.plugins.configureRouting
import com.smartcampus.app.plugins.configureSecurity
import com.smartcampus.app.plugins.configureSerialization
import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureFrameworks()
    configureDatabases()
    configureSerialization()
    configureMonitoring()
    configureSecurity()
    configureRouting()
}
