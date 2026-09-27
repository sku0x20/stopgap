package dev.sku20.stopgap.helidon.authentication

import io.helidon.webserver.http.ServerRequest

interface AuthenticationResolver {
    fun authenticate(request: ServerRequest): Authentication
}
