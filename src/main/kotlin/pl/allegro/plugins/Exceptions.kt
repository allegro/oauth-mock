package pl.allegro.plugins

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import kotlinx.serialization.SerializationException

fun Application.configureExceptions() {
    install(StatusPages) {
        exception<SerializationException> {
            call.respond(HttpStatusCode.BadRequest, it.message.toString())
        }
    }
}