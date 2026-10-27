package dev.sku20.stopgap.helidon.mapper.req.exception

import io.helidon.http.HttpException
import io.helidon.http.Status

class InvalidBoundaryException : HttpException("Multipart boundary is invalid", Status.BAD_REQUEST_400)
