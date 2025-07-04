package com.smartcampus

import com.smartcampus.plugins.configureDatabases
import com.smartcampus.plugins.configureFrameworks
import com.smartcampus.plugins.configureMonitoring
import com.smartcampus.plugins.configureRouting
import com.smartcampus.plugins.configureSecurity
import com.smartcampus.plugins.configureSerialization
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
