package pl.allegro.api

import com.nimbusds.jose.jwk.JWKSet
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.http.*
import io.ktor.server.testing.*
import pl.allegro.plugins.configureRouting

class MultiProvidersTest : StringSpec({
    "should create new provider and return different jwks than for default one"{
        withTestApplication(moduleFunction = { configureRouting() }) {
            val defaultCall = handleRequest(HttpMethod.Get, "/auth/jwks")
            val newProviderCall = handleRequest(HttpMethod.Get, "/new-provider/jwks")

            defaultCall.response.status() shouldBe HttpStatusCode.OK
            newProviderCall.response.status() shouldBe HttpStatusCode.OK

            val defaultJWKs = JWKSet.parse(defaultCall.response.content)
            val newJWKs = JWKSet.parse(newProviderCall.response.content)

            defaultJWKs.keys.first() shouldNotBe newJWKs.keys.first()
        }
    }
})
