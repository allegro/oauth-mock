package pl.allegro.services

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.ktor.features.*
import pl.allegro.client.Client
import pl.allegro.client.ClientsService
import pl.allegro.client.LocalClientsService
import pl.allegro.plugins.UnauthorizedException
import java.time.Instant
import java.util.*


class OAuthService(
    private val clientsService: ClientsService = LocalClientsService()
) {

    private val rsaKey: RSAKey = RSAKeyGenerator(2048).generate()

    fun getJWK() = JWK.parse(rsaKey.toPublicJWK().toJSONObject())

    fun getJWKs() = JWKSet(getJWK())

    fun generateToken(clientId: String? = null, clientSecret: String? = null): String {
        if (clientId == null) return generateToken(createBasicClaims())

        val client = clientsService.getClientWithId(clientId) ?: throw NotFoundException("Client with id $clientId not found")

        if (client.clientSecret != clientSecret) throw UnauthorizedException()

        return generateToken(payload = createClaimsForClient(client))
    }
    fun addClient(client: Client) = clientsService.addClient(client)

    private fun generateToken(payload: JWTClaimsSet): String {
        val header = JWSHeader.Builder(JWSAlgorithm.RS256)
            .type(JOSEObjectType.JWT)
            .build()

        val signedJWT = SignedJWT(header, payload)
        signedJWT.sign(RSASSASigner(rsaKey.toRSAPrivateKey()))
        return signedJWT.serialize()
    }

    private fun createClaimsForClient(client: Client): JWTClaimsSet = JWTClaimsSet.Builder()
        .expirationTime(Date.from(Instant.now().plusSeconds(3600)))
        .jwtID(UUID.randomUUID().toString())
        .claim("client_id", client.clientId)
        .claim("authorities", client.authorities)
        .claim("scope", client.scope)
        .build()

    private fun createBasicClaims(): JWTClaimsSet = JWTClaimsSet.Builder()
        .expirationTime(Date.from(Instant.now().plusSeconds(3600)))
        .jwtID(UUID.randomUUID().toString())
        .claim("client_id", "client")
        .build()

}


