package dev.sku20.stopgap.helidon.mapper

import io.helidon.http.HttpException
import io.helidon.http.Status

class EmptyBodyException : HttpException("Request body is empty", Status.BAD_REQUEST_400)
