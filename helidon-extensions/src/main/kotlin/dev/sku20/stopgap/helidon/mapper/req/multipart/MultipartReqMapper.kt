package dev.sku20.stopgap.helidon.mapper.req.multipart

import dev.sku20.stopgap.helidon.mapper.req.ReqMapper
import dev.sku20.stopgap.helidon.mapper.req.exception.UnsupportedMediaTypeException
import io.helidon.webserver.http.ServerRequest
import io.helidon.webserver.http.ServerResponse
import kotlin.reflect.KType

class MultipartReqMapper : ReqMapper {

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> map(
        request: ServerRequest,
        response: ServerResponse,
        type: KType
    ): T {
        val mediaType = request.headers().contentType().orElse(null) ?: throw UnsupportedMediaTypeException(null)
        val content = request.content().inputStream()
        val multipartRequest = MultipartRequest(mediaType, content)
        return multipartRequest.multiPart() as T
    }

}