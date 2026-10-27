package dev.sku20.stopgap.app.mapper

import dev.sku20.stopgap.helidon.mapper.req.bodyreqmapper.plaintext.PlainTextReqMapper
import dev.sku20.stopgap.helidon.mapper.res.bodyresmapper.plaintext.PlainTextResMapper
import dev.sku20.stopgap.ir.annotation.Creates

object MapperConfig {

    @Creates
    fun mapperEndpoint(): MapperEndpoint {
        return MapperEndpoint()
    }

    @Creates
    fun plainTextReqMapper(): PlainTextReqMapper {
        return PlainTextReqMapper()
    }

    @Creates
    fun plainTextResMapper(): PlainTextResMapper {
        return PlainTextResMapper()
    }

}
