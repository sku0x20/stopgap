package dev.sku20.stopgap.app.httpbin.web

import dev.sku20.stopgap.helidon.endpoint.Endpoint
import dev.sku20.stopgap.helidon.endpoint.Get
import dev.sku20.stopgap.helidon.param.PathParam
import io.helidon.common.buffers.DataReader
import io.helidon.http.Status
import io.helidon.webclient.api.HttpClientResponse
import io.helidon.webclient.api.WebClient
import io.helidon.webserver.http.ServerResponse
import java.io.UncheckedIOException
import java.net.SocketTimeoutException

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

    /**
     * Maps httpbin's status the way an API backed by a dependency would, instead of relaying it.
     *
     * Following RFC 9110's gateway semantics, the caller learns whose fault a failure is, not the dependency's status:
     * - upstream 5xx → `502 Bad Gateway`: we received an invalid response from the server we called
     *   ([§15.6.3](https://www.rfc-editor.org/rfc/rfc9110#section-15.6.3)).
     * - upstream 404 → `404 Not Found`: the one 4xx that means something to the caller, the resource doesn't exist
     *   ([§15.5.5](https://www.rfc-editor.org/rfc/rfc9110#section-15.5.5)).
     * - other upstream 4xx → `500 Internal Server Error`: the caller's request was fine, ours to the dependency wasn't,
     *   so it's our bug ([§15.6.1](https://www.rfc-editor.org/rfc/rfc9110#section-15.6.1)). Relaying a 400 or 401
     *   would tell the caller to fix a request they can't see.
     *
     * Relaying 5xx as-is would also mislead retries and alerts: a relayed 503 reads as "this service is overloaded"
     * and a relayed 500 as "this service has a bug", when the failure is a dependency's.
     */
    @Get("/status-mapped/{code}")
    fun statusMapped(
        @PathParam("code")
        code: String,
        res: ServerResponse
    ) {
        val path = "/status/${parseNumber(code, STATUS_CODES)}"
        send(path).use { upstream ->
            val upstreamStatus = upstream.status()
            if (upstreamStatus == Status.NOT_FOUND_404) {
                throw UpstreamNotFoundException(path)
            }
            when (upstreamStatus.family()) {
                Status.Family.SERVER_ERROR -> throw BadGatewayException("httpbin failed $path with $upstreamStatus")
                Status.Family.CLIENT_ERROR -> throw UpstreamRejectedException(path, upstreamStatus)
                else -> copyTo(upstream, res)
            }
        }
    }

    @Get("/headers")
    fun headers(res: ServerResponse) {
        relay("/headers", res)
    }

    private fun relay(path: String, res: ServerResponse) {
        send(path).use { upstream ->
            copyTo(upstream, res)
        }
    }

    /**
     * Sends the request, turning transport failures into gateway errors instead of Helidon's default 500.
     *
     * Following RFC 9110's gateway semantics, a 500 means a bug in this service, so a failing dependency gets its own
     * status. That keeps alerts on our bugs apart from a dependency being down, and lets clients and load balancers
     * retry, since they commonly retry 502/503/504 but not 500:
     * - no response in time (connect or read timeout) → `504 Gateway Timeout`
     *   ([§15.6.5](https://www.rfc-editor.org/rfc/rfc9110#section-15.6.5)).
     * - unreachable or no valid response (refused, DNS failure, connection dropped) → `502 Bad Gateway`
     *   ([§15.6.3](https://www.rfc-editor.org/rfc/rfc9110#section-15.6.3)).
     *
     * Helidon's WebClient reports these as three unrelated types, found by triggering each:
     * - refused, connect timeout, read timeout: `UncheckedIOException`, with a `SocketTimeoutException` cause for both
     *   timeouts.
     * - DNS failure: `IllegalArgumentException("Failed to get address for host …")`, with no cause.
     * - connection dropped before a response: `DataReader.InsufficientDataAvailableException`.
     *
     * Only `request()` is guarded, so an `IllegalArgumentException` from building the request stays a 500.
     */
    private fun send(path: String): HttpClientResponse {
        val request = httpbinClient.get(path)
        try {
            return request.request()
        } catch (e: UncheckedIOException) {
            if (e.cause is SocketTimeoutException) {
                throw GatewayTimeoutException(path, e)
            }
            throw BadGatewayException("httpbin unreachable: $path", e)
        } catch (e: IllegalArgumentException) {
            throw BadGatewayException("httpbin unreachable: $path", e)
        } catch (e: DataReader.InsufficientDataAvailableException) {
            throw BadGatewayException("httpbin closed the connection: $path", e)
        }
    }

    private fun copyTo(upstream: HttpClientResponse, res: ServerResponse) {
        res.status(upstream.status())
        val contentType = upstream.headers().contentType()
        if (contentType.isPresent) {
            res.headers().contentType(contentType.get())
        }
        res.send(upstream.entity().inputStream().readAllBytes())
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
