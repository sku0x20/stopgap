package dev.sku20.stopgap.helidon.mapper

import io.helidon.webserver.http.ServerRequest
import kotlin.reflect.KType

class JsonRequestMapper : ReqMapper {
    override fun <T> map(request: ServerRequest, type: KType): T = TODO()
}
