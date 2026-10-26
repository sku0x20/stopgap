package dev.sku20.stopgap.helidon.mapper.bodyreqmapper

import dev.sku20.stopgap.helidon.mapper.ReqMapper
import io.helidon.webserver.http.ServerRequest
import kotlin.reflect.KType

class MultiBodyReqMapper(private val mappers: List<MediaTypeReqMapper>) : ReqMapper {
    override fun <T> map(request: ServerRequest, type: KType): T = TODO()
}
