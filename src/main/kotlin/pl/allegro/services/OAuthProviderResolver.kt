package pl.allegro.services

class OAuthProviderResolver {
    private val providers: MutableMap<String, OAuthService> = mutableMapOf()

    init {
        providers["auth"] = OAuthService()
    }

    fun getServiceForProvider(provider: String): OAuthService {
        return if (providers.contains(provider)) {
            providers[provider]!!
        } else {
            addProvider(provider)
        }
    }

    private fun addProvider(name: String): OAuthService {
        providers[name] = OAuthService()
        return providers[name]!!
    }
}