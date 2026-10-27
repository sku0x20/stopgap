package dev.sku20.stopgap.helidon.mapper.res

import io.helidon.webserver.http.ServerResponse
import kotlin.reflect.KType

interface ResMapper {
    fun <T> map(response: ServerResponse, value: T, type: KType)
}
