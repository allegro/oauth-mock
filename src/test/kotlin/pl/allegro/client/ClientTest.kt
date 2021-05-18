package pl.allegro.client

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeEmpty
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class ClientTest : StringSpec({
    "should serialize json without all fields to client"{
        val clientJson = """{"clientId": "only-id"}"""
        val client: Client = Json.decodeFromString(clientJson)

        client.shouldNotBeNull()
        client.clientId shouldBe "only-id"
        client.clientSecret.shouldNotBeNull().shouldNotBeEmpty()
    }
})
