package com.example.stopgap

import io.helidon.webserver.http.ServerRequest
import io.helidon.webserver.http.ServerResponse

class MainEndpoint : Endpoint {

    fun ping(req: ServerRequest, res: ServerResponse) {
        res.send("pong")
    }

}
