package dev.sku20.stopgap.helidon.mapper.req.multipart

import dev.sku20.stopgap.helidon.mapper.req.ReqMapper
import io.helidon.webserver.http.ServerRequest
import kotlin.reflect.KType

class MultipartReqMapper : ReqMapper {
    override fun <T> map(request: ServerRequest, type: KType): T {
        TODO("Not yet implemented")
    }
}
