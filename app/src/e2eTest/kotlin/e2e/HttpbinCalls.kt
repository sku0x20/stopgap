package e2e

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONObject
import dev.sku20.stopgap.helidon.test.InjectInstance
import io.helidon.http.HeaderNames
import io.helidon.http.Status
import io.helidon.webclient.api.WebClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class HttpbinCalls {

    @InjectInstance
    lateinit var client: WebClient

    @Test
    fun delay() {
        val response = client.get("/httpbin/delay/1").request()
        assertThat(response.status()).isEqualTo(Status.OK_200)
    }

    @Test
    fun relaysStatus() {
        val response = client.get("/httpbin/status/503").request()
        assertThat(response.status()).isEqualTo(Status.SERVICE_UNAVAILABLE_503)
    }

    @Test
    fun invalidParam() {
        assertThat(client.get("/httpbin/delay/abc").request().status()).isEqualTo(Status.BAD_REQUEST_400)
        assertThat(client.get("/httpbin/delay/11").request().status()).isEqualTo(Status.BAD_REQUEST_400)
        assertThat(client.get("/httpbin/status/100").request().status()).isEqualTo(Status.BAD_REQUEST_400)
    }

    @Test
    fun propagatesTraceparent() {
        val traceId = newTraceId()
        val response = client.get("/httpbin/headers")
            .header(TRACEPARENT, traceparent(traceId))
            .request()
        assertThat(response.status()).isEqualTo(Status.OK_200)
        assertThat(response.`as`(String::class.java)).contains(traceId)
    }

    @Test
    fun clientSpanUnderServerSpan() {
        val traceId = newTraceId()
        client.get("/httpbin/delay/1").header(TRACEPARENT, traceparent(traceId)).request().close()

        val spans = awaitSpans(traceId)
        val server = spans.first { it.getString("kind") == "SPAN_KIND_SERVER" }
        val httpbin = spans.first { it.getString("kind") == "SPAN_KIND_CLIENT" }
        assertThat(httpbin.getString("parentSpanId")).isEqualTo(server.getString("spanId"))
    }

    // Spans are exported in batches, so the trace shows up in Tempo after a delay; wait for the client span.
    private fun awaitSpans(traceId: String): List<JSONObject> {
        val tempo = WebClient.builder()
            .baseUri(System.getProperty(LgtmContainerRunner.TEMPO_URL_PROPERTY))
            .build()
        val deadline = System.currentTimeMillis() + 30_000
        while (System.currentTimeMillis() < deadline) {
            val response = tempo.get("/api/v2/traces/$traceId").request()
            if (response.status() == Status.OK_200) {
                val spans = spans(response.`as`(String::class.java))
                if (spans.any { it.getString("kind") == "SPAN_KIND_CLIENT" }) {
                    return spans
                }
            } else {
                response.close()
            }
            Thread.sleep(500)
        }
        throw AssertionError("Trace $traceId has no client span in Tempo")
    }

    private fun spans(body: String): List<JSONObject> {
        val spans = mutableListOf<JSONObject>()
        // Tempo answers 200 with an empty trace until it's ingested.
        val resourceSpans = JSON.parseObject(body).getJSONObject("trace").getJSONArray("resourceSpans") ?: return spans
        for (resourceSpan in resourceSpans.toList(JSONObject::class.java)) {
            for (scopeSpan in resourceSpan.getJSONArray("scopeSpans").toList(JSONObject::class.java)) {
                spans.addAll(scopeSpan.getJSONArray("spans").toList(JSONObject::class.java))
            }
        }
        return spans
    }

    private fun newTraceId(): String =
        UUID.randomUUID().toString().replace("-", "")

    private fun traceparent(traceId: String): String =
        "00-$traceId-00f067aa0ba902b7-01"

    private companion object {
        val TRACEPARENT = HeaderNames.create("traceparent")
    }
}
