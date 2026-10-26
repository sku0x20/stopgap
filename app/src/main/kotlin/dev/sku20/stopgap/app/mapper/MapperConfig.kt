package dev.sku20.stopgap.app.mapper

import dev.sku20.stopgap.ir.annotation.Creates

object MapperConfig {

    @Creates
    fun mapperEndpoint(): MapperEndpoint {
        return MapperEndpoint()
    }

}
