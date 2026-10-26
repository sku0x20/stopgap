package dev.sku20.stopgap.helidon.mapper.exception

import io.helidon.http.HttpException
import io.helidon.http.Status

class MalformedBodyException(cause: Throwable) :
    HttpException("Request body is malformed", Status.BAD_REQUEST_400, cause)
