package pl.allegro

import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jwt.SignedJWT
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import pl.allegro.client.LocalClientsService
import pl.allegro.services.OAuthService

class OAuthServiceTest : StringSpec({

    val oAuthService = OAuthService(LocalClientsService())

    "Should create and validate jwt token"{
        val jwks = oAuthService.getJWKs()
        val jwk = jwks.keys.first()
        val token = oAuthService.generateToken()

        SignedJWT.parse(token).verify(RSASSAVerifier(jwk.toRSAKey())) shouldBe true
    }

})
