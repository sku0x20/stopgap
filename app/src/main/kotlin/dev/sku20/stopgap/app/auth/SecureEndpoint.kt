package dev.sku20.stopgap.app.auth

import dev.sku20.stopgap.helidon.endpoint.Endpoint
import dev.sku20.stopgap.helidon.endpoint.Get
import io.helidon.webserver.http.ServerRequest
import io.helidon.webserver.http.ServerResponse

@Endpoint("/secure")
class SecureEndpoint {

    @Get("/closed")
    fun closed(req: ServerRequest, res: ServerResponse) {
        res.send("secret")
    }
}
