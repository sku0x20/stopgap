package dev.sku20.stopgap.helidon.test.integration

import dev.sku20.stopgap.helidon.authentication.Authentication
import dev.sku20.stopgap.helidon.authentication.AuthenticationResolver
import io.helidon.webserver.http.ServerRequest

class TestAuthFakeResolver: AuthenticationResolver {

    lateinit var currentAuth: Authentication
    override fun authenticate(request: ServerRequest): Authentication = currentAuth
}
