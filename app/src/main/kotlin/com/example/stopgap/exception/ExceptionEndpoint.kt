package com.example.stopgap.exception

import com.example.stopgap.Endpoint
import io.helidon.http.HttpException
import io.helidon.http.Status
import io.helidon.webserver.http.ServerRequest
import io.helidon.webserver.http.ServerResponse

class ExceptionEndpoint : Endpoint {

    fun badClientException(req: ServerRequest, res: ServerResponse) {
        throw BadClientException()
    }

    fun viaHttpException(req: ServerRequest, res: ServerResponse) {
        throw HttpException("Bad client request directly", Status.BAD_REQUEST_400)
    }

}
