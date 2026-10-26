package dev.sku20.stopgap.helidon.mapper

import io.helidon.http.HttpMediaType
import io.helidon.webserver.http.ServerRequest
import kotlin.reflect.KType

interface MediaTypeReqMapper {
    fun accepts(mediaType: HttpMediaType?): Boolean
    fun <T> map(request: ServerRequest, type: KType): T
}
