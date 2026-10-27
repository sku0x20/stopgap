package dev.sku20.stopgap.helidon.mapper.res

import io.helidon.webserver.http.ServerRequest
import io.helidon.webserver.http.ServerResponse

interface ResMapper {
    fun <T : Any> map(request: ServerRequest, response: ServerResponse, value: T)
}
