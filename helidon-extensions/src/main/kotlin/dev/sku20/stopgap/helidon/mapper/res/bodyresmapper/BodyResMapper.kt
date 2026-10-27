package dev.sku20.stopgap.helidon.mapper.res.bodyresmapper

import dev.sku20.stopgap.helidon.mapper.res.ResMapper
import io.helidon.http.HttpMediaType
import io.helidon.webserver.http.ServerRequest
import io.helidon.webserver.http.ServerResponse

class BodyResMapper(
    private val mediaType: HttpMediaType,
    private val encoder: BodyEncoder,
) : ResMapper {
    override fun <T> map(request: ServerRequest, response: ServerResponse, value: T) {
        response.headers().contentType(mediaType)
        response.send(encoder.encode(value))
    }
}
