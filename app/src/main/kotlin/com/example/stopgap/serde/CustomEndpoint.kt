package com.example.stopgap.serde

import dev.sku20.helidon.endpoint.Endpoint
import dev.sku20.helidon.endpoint.Get
import dev.sku20.helidon.serde.CustomSerdeCatalog

@Endpoint("/custom-serde-catalog")
class CustomEndpoint {

    @Get("/")
    @CustomSerdeCatalog(qualifier = "com.example.stopgap.serde.custom")
    fun get(): ResDto {
        return ResDto("hello")
    }
}
