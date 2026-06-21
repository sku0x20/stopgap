package com.example.stopgap.serde

import dev.sku20.helidon.endpoint.Endpoint
import dev.sku20.helidon.endpoint.Get

@Endpoint("/custom-serde-catalog")
class CustomEndpoint {

    @Get("/")
    fun get(): ResDto {
        return ResDto("hello")
    }
}
