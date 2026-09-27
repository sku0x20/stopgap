package dev.sku20.stopgap.app.auth

import dev.sku20.stopgap.helidon.endpoint.Endpoint
import dev.sku20.stopgap.helidon.endpoint.Get

@Endpoint("/secure")
class SecureEndpoint {

    @Get("/closed")
    fun closed(): String {
        return "secret"
    }
}
