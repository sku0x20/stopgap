package com.example.stopgap.serde

import com.alibaba.fastjson2.to
import dev.sku20.helidon.endpoint.Endpoint
import dev.sku20.helidon.endpoint.Post
import io.helidon.webserver.http.ServerRequest
import io.helidon.webserver.http.ServerResponse

@Endpoint("/serde")
class SerdeEndpoint {

    @Post("/desObj")
    fun desObj(
        req: ServerRequest,
        res: ServerResponse
    ) {
        val bytes = req.content().inputStream().readAllBytes()
        val obj = bytes.to<ReqDto>()
        res.send(obj.expected)
    }

}