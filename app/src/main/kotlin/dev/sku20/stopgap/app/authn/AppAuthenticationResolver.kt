package dev.sku20.stopgap.app.authn

import dev.sku20.stopgap.helidon.authentication.Authentication
import dev.sku20.stopgap.helidon.authentication.AuthenticationResolver
import io.helidon.http.HeaderNames
import io.helidon.webserver.http.ServerRequest

class AppAuthenticationResolver : AuthenticationResolver {

    override fun authenticate(request: ServerRequest): Authentication {
        if (request.headers().contains(HeaderNames.AUTHORIZATION)) {
            val authHeader = request.headers().get(HeaderNames.AUTHORIZATION).get()
            if (authHeader.startsWith("Bearer ")) {
                val token = authHeader.removePrefix("Bearer ").trim()
                if (token.isNotEmpty()) {
                    return UserAuthN(id = token)
                }
            }
        }
        return AnonymousAuthN
    }
}
