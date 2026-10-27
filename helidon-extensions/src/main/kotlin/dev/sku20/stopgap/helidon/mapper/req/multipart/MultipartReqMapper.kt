package dev.sku20.stopgap.helidon.mapper.req.multipart

import dev.sku20.stopgap.helidon.mapper.req.ReqMapper
import dev.sku20.stopgap.helidon.mapper.req.exception.EmptyBodyException
import dev.sku20.stopgap.helidon.mapper.req.exception.MissingBoundaryException
import dev.sku20.stopgap.helidon.mapper.req.exception.UnsupportedMediaTypeException
import io.helidon.http.HttpMediaType
import io.helidon.webserver.http.ServerRequest
import kotlin.reflect.KType

class MultipartReqMapper : ReqMapper {
    @Suppress("UNCHECKED_CAST")
    override fun <T> map(request: ServerRequest, type: KType): T {
        val boundary = boundary(request)
        val bytes = request.content().inputStream().readAllBytes()
        if (bytes.isEmpty()) {
            throw EmptyBodyException()
        }
        return MultiPartParser(bytes, boundary).parse() as T
    }

    private fun boundary(request: ServerRequest): String {
        val contentType = request.headers().contentType().orElse(null)
        if (!isFormData(contentType)) {
            throw UnsupportedMediaTypeException(contentType)
        }
        val boundary = contentType!!.parameters()["boundary"]
        if (boundary.isNullOrEmpty()) {
            throw MissingBoundaryException()
        }
        return boundary
    }

    // Compares type and subtype directly: HttpMediaType.test() treats wildcards as a match.
    private fun isFormData(contentType: HttpMediaType?): Boolean {
        val type = contentType?.mediaType() ?: return false
        return type.type().equals("multipart", ignoreCase = true) &&
            type.subtype().equals("form-data", ignoreCase = true)
    }
}
