package dev.sku20.stopgap.app.auth

import dev.sku20.stopgap.helidon.authentication.Authentication
import dev.sku20.stopgap.helidon.authentication.AuthenticationResolver
import dev.sku20.stopgap.ir.Creates
import io.helidon.webserver.http.ServerRequest

object AuthConfig {

    @Creates
    fun secureEndpoint(): SecureEndpoint {
        return SecureEndpoint()
    }

    @Creates
    fun authenticationResolver(): AuthenticationResolver {
        return object : AuthenticationResolver {
            override fun authenticate(request: ServerRequest): Authentication {
                TODO()
            }
        }
    }
}
