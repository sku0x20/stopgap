package dev.sku20.stopgap.helidon.mapper.res

import io.helidon.webserver.http.ServerResponse

interface ResMapper {
    fun <T> map(response: ServerResponse, value: T)
}
