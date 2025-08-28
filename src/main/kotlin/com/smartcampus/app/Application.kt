package com.smartcampus.app

import com.smartcampus.app.plugins.*
import io.ktor.server.application.*
import io.ktor.server.netty.*

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
