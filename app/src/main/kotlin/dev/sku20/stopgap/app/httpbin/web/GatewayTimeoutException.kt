package dev.sku20.stopgap.app.httpbin.web

import io.helidon.http.HttpException
import io.helidon.http.Status

class GatewayTimeoutException(path: String, cause: Throwable) :
    HttpException("httpbin timed out: $path", Status.GATEWAY_TIMEOUT_504, cause)
