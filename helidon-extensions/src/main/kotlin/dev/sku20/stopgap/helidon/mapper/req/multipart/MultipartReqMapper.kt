package dev.sku20.stopgap.helidon.mapper.req.multipart

import dev.sku20.stopgap.helidon.mapper.req.ReqMapper
import dev.sku20.stopgap.helidon.mapper.req.exception.UnsupportedMediaTypeException
import io.helidon.http.HttpMediaType
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
        val mediaType = validContentType(request)
        val content = request.content().inputStream()
        val multipartRequest = MultipartRequest(mediaType, content)
        return multipartRequest.multiPart() as T
    }

    private fun validContentType(request: ServerRequest): HttpMediaType {
        val mediaType = request.headers().contentType().orElse(null) ?: throw UnsupportedMediaTypeException(null)
        if (!isFormData(mediaType)) throw UnsupportedMediaTypeException(mediaType)
        return mediaType
    }

    // Compares type and subtype directly: HttpMediaType.test() treats wildcards as a match.
    private fun isFormData(mediaType: HttpMediaType): Boolean =
        mediaType.mediaType().type().equals("multipart", ignoreCase = true) &&
            mediaType.mediaType().subtype().equals("form-data", ignoreCase = true)

}