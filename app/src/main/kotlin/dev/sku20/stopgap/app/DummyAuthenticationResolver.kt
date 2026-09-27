package dev.sku20.stopgap.app

import dev.sku20.stopgap.helidon.authentication.Authentication
import dev.sku20.stopgap.helidon.authentication.AuthenticationResolver
import io.helidon.webserver.http.ServerRequest

class DummyAuthenticationResolver : AuthenticationResolver {
    override fun authenticate(request: ServerRequest): Authentication {
        TODO()
    }
}
