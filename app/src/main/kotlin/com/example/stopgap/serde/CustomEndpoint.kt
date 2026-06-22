package com.example.stopgap.serde

import dev.sku20.helidon.endpoint.Endpoint
import dev.sku20.helidon.endpoint.Get
import dev.sku20.helidon.serde.CustomSerdeCatalog
import dev.sku20.helidon.serde.SerdeExtras

@Endpoint("/custom-serde-catalog")
@CustomSerdeCatalog(clazz = PlainTextSerdeCatalog::class)
class CustomEndpoint {

    @Get("/")
    fun get(): ResDto {
        return ResDto("hello")
    }

    @Get("/json")
    @CustomSerdeCatalog(qualifier = SerdeExtras.DEFAULT_CATALOG_QUALIFIER)
    fun getJson(): ResDto {
        return ResDto("hello-json")
    }
}
