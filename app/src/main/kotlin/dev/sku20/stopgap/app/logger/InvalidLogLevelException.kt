package dev.sku20.stopgap.app.logger

import io.helidon.http.HttpException
import io.helidon.http.Status

class InvalidLogLevelException(level: String) :
    HttpException("Invalid log level: $level", Status.BAD_REQUEST_400)
