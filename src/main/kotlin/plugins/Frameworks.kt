package com.smartcampus.plugins

import com.smartcampus.di.appModule
import com.smartcampus.di.authModule
import com.smartcampus.di.coreModule
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureFrameworks() {
    install(Koin) {
        slf4jLogger()
        modules(appModule(this@configureFrameworks.environment), coreModule, authModule)
//        modules(module {
//            single<HelloService> {
//                HelloService {
//                    println(environment.log.info("Hello, World!"))
//                }
//            }
//        })
    }
}
