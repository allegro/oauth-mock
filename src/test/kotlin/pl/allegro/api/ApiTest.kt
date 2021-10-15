package pl.allegro.api

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jwt.JWTParser
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import pl.allegro.client.Client
import pl.allegro.plugins.configureExceptions
import pl.allegro.plugins.configureRouting

class ApiTest : StringSpec({
    "should return jwk key set"{
        withMyTestApplication {
            val call = handleRequest(HttpMethod.Get, "/auth/jwks")

            call.response.status() shouldBe HttpStatusCode.OK
            JWKSet.parse(call.response.content)
        }
    }

    "should return default token"{
        withMyTestApplication{
            val call = handleRequest(HttpMethod.Get, "/auth/token")

            call.response.status() shouldBe HttpStatusCode.OK
            JWTParser.parse(call.response.content)
        }
    }

    "should return token for client"{
        withMyTestApplication{
            val call = getTokenForClient("client1", "secret-one")

            val token = JWTParser.parse(call.response.content)
            val scopes = token.jwtClaimsSet.claims["scope"] as List<*>
            val authorities = token.jwtClaimsSet.claims["authorities"] as List<*>

            call.response.status() shouldBe HttpStatusCode.OK
            scopes shouldContain "access:read"
            authorities shouldContainAll listOf("groupA", "groupB")
        }
    }

    "should return Unauthorized for incorrect secret" {
        withMyTestApplication {
            val call = getTokenForClient("client1", "wrong-secret")
            call.response.status() shouldBe HttpStatusCode.Unauthorized
        }
    }

    "should return Unauthorized for request without secret" {
        withMyTestApplication{
            val call = handleRequest(HttpMethod.Post, "/auth/token") {
                addHeader("content-type", "application/x-www-form-urlencoded")
                setBody("client_id=client1")
            }
            call.response.status() shouldBe HttpStatusCode.Unauthorized
        }
    }

    "should return Not Found if client is not in memory"{
        withMyTestApplication{
            val call = getTokenForClient("non-existent", "non-existent")
            call.response.status() shouldBe HttpStatusCode.NotFound
        }
    }

    "should add client"{
        withMyTestApplication{
            val call = handleRequest(HttpMethod.Put, "/auth/client") {
                addHeader("content-type", "application/json")
                setBody(Json.encodeToString(Client("client-new", "secret")))
            }
            call.response.status() shouldBe HttpStatusCode.Accepted

            with(getTokenForClient("client-new", "secret")) {
                response.status() shouldBe HttpStatusCode.OK
            }
        }
    }

    "should add client with client id only"{
        withMyTestApplication{
            val call = handleRequest(HttpMethod.Put, "/auth/client") {
                addHeader("content-type", "application/json")
                setBody("""{"clientId":"only-id"}""")
            }
            call.response.status() shouldBe HttpStatusCode.Accepted
            with(getTokenForClient("only-id", Json.decodeFromString<Client>(call.response.content!!).clientSecret)) {
                response.status() shouldBe HttpStatusCode.OK
            }
        }
    }
})

private fun TestApplicationEngine.getTokenForClient(clientId: String, clientSecret: String): TestApplicationCall {
    return handleRequest(HttpMethod.Post, "/auth/token") {
        addHeader("content-type", "application/x-www-form-urlencoded")
        setBody("client_id=$clientId&client_secret=$clientSecret")
    }
}

fun <R> withMyTestApplication(test: TestApplicationEngine.() -> R) {
    withTestApplication(moduleFunction = { configureRouting(); configureExceptions() }, test)
}