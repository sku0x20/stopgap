package dev.sku20.stopgap.helidon.mapper.req.multipart

import dev.sku20.stopgap.helidon.mapper.req.ReqMapper
import dev.sku20.stopgap.helidon.mapper.req.exception.MissingBoundaryException
import dev.sku20.stopgap.helidon.mapper.req.exception.UnsupportedMediaTypeException
import io.helidon.http.HttpMediaType
import io.helidon.webserver.http.ServerRequest
import kotlin.reflect.KType

class MultipartReqMapper : ReqMapper {
    override fun <T> map(request: ServerRequest, type: KType): T {
        val contentType = request.headers().contentType().orElse(null)
        if (contentType == null || !isFormData(contentType)) {
            throw UnsupportedMediaTypeException(contentType)
        }
        val boundary = contentType.parameters()["boundary"]
        if (boundary.isNullOrEmpty()) {
            throw MissingBoundaryException()
        }
        TODO("Not yet implemented")
    }

    // Compares type and subtype directly: HttpMediaType.test() treats wildcards as a match.
    private fun isFormData(contentType: HttpMediaType): Boolean {
        val type = contentType.mediaType()
        return type.type().equals("multipart", ignoreCase = true) &&
            type.subtype().equals("form-data", ignoreCase = true)
    }
}
