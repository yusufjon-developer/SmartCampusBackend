package com.smartcampus.app.utils

import com.smartcampus.app.plugins.accessControlService
import io.ktor.server.application.*
import io.ktor.server.routing.*

/**
 * Проверить доступ в рамках текущего вызова (ApplicationCall).
 * AccessControlService ответит 401/403 самостоятельно, если нужно.
 */
suspend fun ApplicationCall.ensureAccess(vararg requiredPermissions: String): Boolean {
    return application.accessControlService.hasAccess(this, *requiredPermissions)
}

/**
 * Обёртки для маршрутов. Ваша версия Ktor использует RoutingContext как receiver
 * в лямбдах-обработчиках, поэтому body: suspend RoutingContext.() -> Unit
 * Внутри мы сначала проверяем доступ, затем вызываем body с текущим контекстом.
 */

fun Route.getWithAccess(path: String? = null, vararg requiredPermissions: String, body: suspend RoutingContext.() -> Unit) {
    if (path == null) {
        get {
            if (!call.ensureAccess(*requiredPermissions)) return@get
            body.invoke(this)
        }
    } else {
        get(path) {
            if (!call.ensureAccess(*requiredPermissions)) return@get
            body.invoke(this)
        }
    }
}

fun Route.postWithAccess(path: String? = null, vararg requiredPermissions: String, body: suspend RoutingContext.() -> Unit) {
    if (path == null) {
        post {
            if (!call.ensureAccess(*requiredPermissions)) return@post
            body.invoke(this)
        }
    } else {
        post(path) {
            if (!call.ensureAccess(*requiredPermissions)) return@post
            body.invoke(this)
        }
    }
}

fun Route.putWithAccess(path: String? = null, vararg requiredPermissions: String, body: suspend RoutingContext.() -> Unit) {
    if (path == null) {
        put {
            if (!call.ensureAccess(*requiredPermissions)) return@put
            body.invoke(this)
        }
    } else {
        put(path) {
            if (!call.ensureAccess(*requiredPermissions)) return@put
            body.invoke(this)
        }
    }
}

fun Route.deleteWithAccess(path: String? = null, vararg requiredPermissions: String, body: suspend RoutingContext.() -> Unit) {
    if (path == null) {
        delete {
            if (!call.ensureAccess(*requiredPermissions)) return@delete
            body.invoke(this)
        }
    } else {
        delete(path) {
            if (!call.ensureAccess(*requiredPermissions)) return@delete
            body.invoke(this)
        }
    }
}
