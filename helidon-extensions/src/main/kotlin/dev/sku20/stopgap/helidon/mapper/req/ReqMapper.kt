package dev.sku20.stopgap.helidon.mapper.req

import io.helidon.webserver.http.ServerRequest
import io.helidon.webserver.http.ServerResponse
import kotlin.reflect.KType

interface ReqMapper {
    fun <T> map(request: ServerRequest, response: ServerResponse, type: KType): T
}
