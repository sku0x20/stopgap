package e2e

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONObject
import dev.sku20.stopgap.helidon.test.InjectInstance
import io.helidon.common.media.type.MediaTypes
import io.helidon.http.HeaderNames
import io.helidon.http.Status
import io.helidon.webclient.api.WebClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class MetricsCalls {

    @InjectInstance
    lateinit var client: WebClient

    private val prometheus = WebClient.builder()
        .baseUri(System.getProperty(LgtmContainerRunner.PROMETHEUS_URL_PROPERTY))
        .build()

    // A mean near 1s for a 1s delay checks the unit too: Helidon's legacy filter records ms into a seconds histogram.
    @Test
    fun serverDuration() {
        client.get("/httpbin/delay/1").request().close()

        val series = """{http_route="/httpbin/delay/{seconds}", http_response_status_code="200"}"""
        val mean = awaitValue(
            "sum(http_server_request_duration_seconds_sum$series) / sum(http_server_request_duration_seconds_count$series)"
        )
        assertThat(mean).isBetween(1.0, 5.0)
    }

    // url.template is the raw path: Helidon's client metrics have no route template to use.
    @Test
    fun clientDuration() {
        client.get("/httpbin/delay/1").request().close()

        val series = """{url_template="/delay/1", http_response_status_code="200"}"""
        val mean = awaitValue(
            "sum(http_client_request_duration_seconds_sum$series) / sum(http_client_request_duration_seconds_count$series)"
        )
        assertThat(mean).isBetween(1.0, 5.0)
    }

    @Test
    fun connectionPool() {
        assertThat(awaitValue("sum(db_client_connection_count)")).isPositive()
    }

    @Test
    fun notesCreated() {
        val response = client.post("/notes")
            .contentType(MediaTypes.APPLICATION_JSON)
            .accept(MediaTypes.APPLICATION_JSON)
            .submit("""{"text":"metrics"}""")
        assertThat(response.status()).isEqualTo(Status.OK_200)

        assertThat(awaitValue("sum(notes_created_total)")).isPositive()
    }

    @Test
    fun jvmRuntime() {
        assertThat(awaitValue("sum(jvm_thread_count)")).isPositive()
    }

    // Helidon's Micrometer meters reach Prometheus only through the OTel bridge.
    @Test
    fun micrometerMeters() {
        client.get("/").request().close()

        assertThat(awaitValue("sum(requests_count_total)")).isPositive()
        assertThat(awaitValue("sum(thread_count)")).isPositive()
    }

    // service.version comes from the jar manifest, deployment.environment.name from OTEL_RESOURCE_ATTRIBUTES; LGTM's
    // Prometheus promotes both to labels.
    @Test
    fun resourceAttributes() {
        val series = """{service_version=~"[0-9a-f]{7,}", deployment_environment_name="e2e"}"""
        assertThat(awaitValue("count(jvm_thread_count$series)")).isPositive()
    }

    // An exemplar is the trace id of a sampled request, stored next to the histogram bucket its value fell in; the SDK
    // attaches one only if the span is current while the value is recorded.
    @Test
    fun serverDurationExemplar() {
        val traceId = newTraceId()
        client.get("/httpbin/delay/1").header(TRACEPARENT, traceparent(traceId)).request().close()

        awaitExemplar("http_server_request_duration_seconds_bucket", traceId)
    }

    @Test
    fun dbDurationExemplar() {
        val traceId = newTraceId()
        val response = client.post("/notes")
            .header(TRACEPARENT, traceparent(traceId))
            .contentType(MediaTypes.APPLICATION_JSON)
            .accept(MediaTypes.APPLICATION_JSON)
            .submit("""{"text":"exemplar"}""")
        assertThat(response.status()).isEqualTo(Status.OK_200)

        awaitExemplar("db_client_operation_duration_seconds_bucket", traceId)
    }

    @Test
    fun noScrapeEndpoint() {
        val response = client.get("/observe/metrics").request()
        assertThat(response.status()).isEqualTo(Status.NOT_FOUND_404)
    }

    // Metrics are exported every 2s in e2e and Prometheus answers an empty result until a series is ingested.
    private fun awaitValue(query: String): Double {
        val deadline = System.currentTimeMillis() + 30_000
        while (System.currentTimeMillis() < deadline) {
            val result = JSON.parseObject(get("/api/v1/query", query)).getJSONObject("data").getJSONArray("result")
            if (result.isNotEmpty()) {
                return result.getJSONObject(0).getJSONArray("value").getString(1).toDouble()
            }
            Thread.sleep(500)
        }
        throw AssertionError("No result for $query; metric names: ${get("/api/v1/label/__name__/values", null)}")
    }

    private fun awaitExemplar(metric: String, traceId: String) {
        val deadline = System.currentTimeMillis() + 30_000
        while (System.currentTimeMillis() < deadline) {
            if (traceId in exemplarTraceIds(metric)) {
                return
            }
            Thread.sleep(500)
        }
        throw AssertionError("No $metric exemplar for trace $traceId; trace ids: ${exemplarTraceIds(metric)}")
    }

    private fun exemplarTraceIds(metric: String): List<String> {
        val traceIds = mutableListOf<String>()
        val series = JSON.parseObject(get("/api/v1/query_exemplars", metric)).getJSONArray("data")
        for (exemplars in series.toList(JSONObject::class.java)) {
            for (exemplar in exemplars.getJSONArray("exemplars").toList(JSONObject::class.java)) {
                traceIds.add(exemplar.getJSONObject("labels").getString("trace_id"))
            }
        }
        return traceIds
    }

    private fun newTraceId(): String =
        UUID.randomUUID().toString().replace("-", "")

    private fun traceparent(traceId: String): String =
        "00-$traceId-00f067aa0ba902b7-01"

    private fun get(path: String, query: String?): String {
        val request = prometheus.get(path)
        if (query != null) {
            request.queryParam("query", query)
        }
        val response = request.request()
        assertThat(response.status()).isEqualTo(Status.OK_200)
        return response.`as`(String::class.java)
    }

    private companion object {
        val TRACEPARENT = HeaderNames.create("traceparent")
    }
}
