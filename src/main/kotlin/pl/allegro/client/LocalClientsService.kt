package pl.allegro.client

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory

interface ClientsService {
    fun addClient(client: Client): Client?
    fun getClientWithId(clientId: String): Client?
    fun getAllClients(): Collection<Client> = emptyList()
}

class LocalClientsService : ClientsService {
    private val clients: MutableSet<Client> = mutableSetOf()

    init {
        addClientsFromResources()
    }

    override fun addClient(client: Client): Client? {
        clients.add(client)
        return clients.firstOrNull { it.clientId == client.clientId }
    }

    override fun getClientWithId(clientId: String) = clients.firstOrNull { it.clientId == clientId }
    override fun getAllClients(): Collection<Client> = clients

    private fun addClientsFromResources() {
        javaClass.getResource("/clients.json")?.readText()?.let { jsonString ->
            Json.decodeFromString<List<Client>>(jsonString).map { clients.add(it) }
        } ?: log.warn("Clients from clients.json loading failed.")
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(LocalClientsService::class.java)
    }
}

