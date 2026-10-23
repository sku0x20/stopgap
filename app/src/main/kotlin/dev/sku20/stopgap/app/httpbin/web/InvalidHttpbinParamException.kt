package dev.sku20.stopgap.app.httpbin.web

import io.helidon.http.HttpException
import io.helidon.http.Status

class InvalidHttpbinParamException(value: String) :
    HttpException("Invalid httpbin param: $value", Status.BAD_REQUEST_400)
