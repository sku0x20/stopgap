package dev.sku20.stopgap.helidon.mapper.req.exception

import io.helidon.http.HttpException
import io.helidon.http.Status

class MissingBoundaryException : HttpException("Multipart boundary is missing", Status.BAD_REQUEST_400)
