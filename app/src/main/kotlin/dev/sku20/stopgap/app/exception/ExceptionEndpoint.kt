package dev.sku20.stopgap.app.exception

import dev.sku20.stopgap.helidon.endpoint.Endpoint
import dev.sku20.stopgap.helidon.endpoint.Get
import dev.sku20.stopgap.helidon.endpoint.Post
import io.helidon.http.HttpException
import io.helidon.http.Status
import io.helidon.webserver.http.ServerRequest
import io.helidon.webserver.http.ServerResponse

@Endpoint("/exception")
class ExceptionEndpoint {

    @Get("/bad-client-1")
    fun badClientException(req: ServerRequest, res: ServerResponse) {
        throw BadClientException()
    }

    @Get("/bad-client-2")
    fun viaHttpException(req: ServerRequest, res: ServerResponse) {
        throw HttpException("Bad client request directly", Status.BAD_REQUEST_400)
    }

    @Post("/bad-post")
    fun post(req: ServerRequest, res: ServerResponse) {
        throw HttpException("Bad Request", Status.BAD_REQUEST_400)
    }

}
