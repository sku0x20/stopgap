package com.example.stopgap.serde

import com.alibaba.fastjson2.to
import com.alibaba.fastjson2.toJSONByteArray
import dev.sku20.helidon.endpoint.Endpoint
import dev.sku20.helidon.endpoint.Get
import dev.sku20.helidon.endpoint.Post
import io.helidon.http.HeaderNames
import io.helidon.http.HttpMediaTypes
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

    @Get("/serObj")
    fun serObj(
        req: ServerRequest,
        res: ServerResponse
    ) {
        val query = req.query()
        val data = query.get("data")

        val obj = ResDto(data)
        res.header(HeaderNames.CONTENT_TYPE, HttpMediaTypes.JSON_UTF_8.text())
        res.send(obj.toJSONByteArray())
    }

}