package dev.sku20.stopgap.app.httpbin.web

import io.helidon.http.HttpException
import io.helidon.http.Status

class BadGatewayException(message: String, cause: Throwable? = null) :
    HttpException(message, Status.BAD_GATEWAY_502, cause)
