package pl.allegro.client

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeIn

class ClientsServiceTest : StringSpec({
    val clientsService = LocalClientsService()

    "should load clients from clients.json"{
        val clients = clientsService.getAllClients()
        clients.map {
            it.clientId shouldBeIn listOf("client1", "client2")
        }
    }
})
