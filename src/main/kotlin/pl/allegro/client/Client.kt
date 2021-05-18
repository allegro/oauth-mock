package pl.allegro.client

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Client(
    val clientId: String,
    val clientSecret: String = UUID.randomUUID().toString(),
    val authorities: List<String> = emptyList(),
    val scope: List<String> = emptyList()
)