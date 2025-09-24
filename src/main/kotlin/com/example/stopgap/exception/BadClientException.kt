package com.example.stopgap.exception

import io.helidon.http.HttpException
import io.helidon.http.Status

// i like this more than registering an error handler in the webserver
class BadClientException : HttpException(
    "Bad client request by extending",
    Status.BAD_REQUEST_400
)
