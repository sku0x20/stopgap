package com.example.stopgap.serde

import com.example.stopgap.serde.plain.PlainTextSerdeCatalog
import dev.sku20.stopgap.helidon.endpoint.Endpoint
import dev.sku20.stopgap.helidon.endpoint.Get
import dev.sku20.stopgap.helidon.serde.CustomSerdeCatalog
import dev.sku20.stopgap.helidon.serde.SerdeExtras

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
