package pl.allegro.api

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jwt.JWTParser
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import pl.allegro.client.Client
import pl.allegro.plugins.configureRouting

class ApiTest : StringSpec({
    "should return jwk key set"{
        withTestApplication(moduleFunction = { configureRouting() }) {
            val call = handleRequest(HttpMethod.Get, "/auth/jwks")

            call.response.status() shouldBe HttpStatusCode.OK
            JWKSet.parse(call.response.content)
        }
    }

    "should return default token"{
        withTestApplication(moduleFunction = { configureRouting() }) {
            val call = handleRequest(HttpMethod.Get, "/auth/token")

            call.response.status() shouldBe HttpStatusCode.OK
            JWTParser.parse(call.response.content)
        }
    }

    "should return token for client"{
        withTestApplication(moduleFunction = { configureRouting() }) {
            val call = getTokenForClient("client1")

            val token = JWTParser.parse(call.response.content)
            val scopes = token.jwtClaimsSet.claims["scope"] as List<*>
            val authorities = token.jwtClaimsSet.claims["authorities"] as List<*>

            call.response.status() shouldBe HttpStatusCode.OK
            scopes shouldContain "access:read"
            authorities shouldContainAll listOf("groupA", "groupB")
        }
    }

    "should return Not Found if client is not in memory"{
        withTestApplication(moduleFunction = { configureRouting() }) {
            val call = getTokenForClient("non-existent")
            call.response.status() shouldBe HttpStatusCode.NotFound
        }
    }

    "should add client"{
        withTestApplication(moduleFunction = { configureRouting() }) {
            val call = handleRequest(HttpMethod.Put, "/auth/client") {
                addHeader("content-type", "application/json")
                setBody(Json.encodeToString(Client("client-new", "secret")))
            }
            call.response.status() shouldBe HttpStatusCode.Accepted

            with(getTokenForClient("client-new")) {
                response.status() shouldBe HttpStatusCode.OK
            }
        }
    }

    "should add client with client id only"{
        withTestApplication(moduleFunction = { configureRouting() }) {
            val call = handleRequest(HttpMethod.Put, "/auth/client") {
                addHeader("content-type", "application/json")
                setBody("""{"clientId":"only-id"}""")
            }
            call.response.status() shouldBe HttpStatusCode.Accepted

            with(getTokenForClient("only-id")) {
                response.status() shouldBe HttpStatusCode.OK
            }
        }
    }
})

private fun TestApplicationEngine.getTokenForClient(clientId: String): TestApplicationCall {
    return handleRequest(HttpMethod.Post, "/auth/token") {
        addHeader("content-type", "application/x-www-form-urlencoded")
        setBody("client_id=$clientId")
    }
}