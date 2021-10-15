package pl.allegro.plugins

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import pl.allegro.client.Client
import pl.allegro.services.OAuthProviderResolver


fun Application.configureRouting() {
    val providerResolver = OAuthProviderResolver()

    routing {
        get("/") {
            call.respond(HttpStatusCode.OK)
        }
        get("/{provider}/jwks") {
            log.info("Received jwks request. User-Agent: [{}]", call.request.userAgent())
            call.respondText(
                providerResolver.getServiceForProvider(call.parameters.getProvider()).getJWKs().toPublicJWKSet()
                    .toString(),
                ContentType.parse("application/json")
            )
        }
        post("/{provider}/token") {
            val params = call.receiveParameters()
            val oAuthService = providerResolver.getServiceForProvider(call.parameters.getProvider())
            call.respondText(oAuthService.generateToken(params["client_id"], params["client_secret"]))
        }
        get("/{provider}/token") {
            call.respondText(providerResolver.getServiceForProvider(call.parameters.getProvider()).generateToken())
        }
        put("/{provider}/client") {
            val clientString = call.receive<String>()
            val client = providerResolver.getServiceForProvider(call.parameters.getProvider())
                .addClient(Json.decodeFromString<Client>(clientString))

            call.respond(HttpStatusCode.Accepted, Json.encodeToString(client))
        }
    }
}

fun Parameters.getProvider() = this["provider"] ?: "auth"