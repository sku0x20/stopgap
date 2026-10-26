package dev.sku20.stopgap.helidon.mapper.req

import io.helidon.webserver.http.ServerRequest
import kotlin.reflect.KType

interface ReqMapper {
    fun <T> map(request: ServerRequest, type: KType): T
}
