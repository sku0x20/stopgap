package dev.sku20.stopgap.app.httpbin.web

import dev.sku20.stopgap.helidon.endpoint.Endpoint
import dev.sku20.stopgap.helidon.endpoint.Get
import dev.sku20.stopgap.helidon.param.PathParam
import io.helidon.webclient.api.WebClient
import io.helidon.webserver.http.ServerResponse

// Relays httpbin's status and body as-is, so slow and failing upstream calls can be seen in traces and logs.
@Endpoint("/httpbin")
class HttpbinEndpoint(
    private val httpbinClient: WebClient
) {

    @Get("/delay/{seconds}")
    fun delay(
        @PathParam("seconds")
        seconds: String,
        res: ServerResponse
    ) {
        relay("/delay/${parseNumber(seconds, DELAY_SECONDS)}", res)
    }

    @Get("/status/{code}")
    fun status(
        @PathParam("code")
        code: String,
        res: ServerResponse
    ) {
        relay("/status/${parseNumber(code, STATUS_CODES)}", res)
    }

    @Get("/headers")
    fun headers(res: ServerResponse) {
        relay("/headers", res)
    }

    private fun relay(path: String, res: ServerResponse) {
        httpbinClient.get(path).request().use { upstream ->
            res.status(upstream.status())
            val contentType = upstream.headers().contentType()
            if (contentType.isPresent) {
                res.headers().contentType(contentType.get())
            }
            res.send(upstream.entity().inputStream().readAllBytes())
        }
    }

    // Parsed, not passed through: a raw segment like `..%2Fanything` would reach other httpbin paths.
    private fun parseNumber(value: String, range: IntRange): Int {
        val number = value.toIntOrNull()
        if (number == null || number !in range) {
            throw InvalidHttpbinParamException(value)
        }
        return number
    }

    private companion object {
        // go-httpbin caps delays at 10s; the client's read timeout must stay above this.
        val DELAY_SECONDS = 0..10

        // 1xx left out: informational, not a final response to relay.
        val STATUS_CODES = 200..599
    }
}
