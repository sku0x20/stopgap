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

class ProfilesCalls {

    @InjectInstance
    lateinit var client: WebClient

    // The profiler uploads every 2s in e2e; the CPU type shows up once a batch with samples is ingested.
    @Test
    fun cpuProfile() {
        val deadline = System.currentTimeMillis() + 30_000
        while (System.currentTimeMillis() < deadline) {
            client.get("/").request().close()
            if (CPU_PROFILE_TYPE in profileTypes()) {
                return
            }
            Thread.sleep(500)
        }
        throw AssertionError("No $CPU_PROFILE_TYPE profile for stopgap; types: ${profileTypes()}")
    }

    // The extension's span processor tags the root span, which is how Grafana finds the span's profile.
    @Test
    fun serverSpanProfileId() {
        val traceId = UUID.randomUUID().toString().replace("-", "")
        client.get("/").header(TRACEPARENT, "00-$traceId-00f067aa0ba902b7-01").request().close()

        val server = awaitServerSpan(traceId)
        assertThat(attribute(server, "pyroscope.profile.id")).isNotBlank()
    }

    private fun profileTypes(): List<String> {
        val pyroscope = WebClient.builder()
            .baseUri(System.getProperty(LgtmContainerRunner.PYROSCOPE_URL_PROPERTY))
            .build()
        val now = System.currentTimeMillis()
        val request = JSONObject.of(
            "name", "__profile_type__",
            "matchers", listOf("""{service_name="stopgap"}"""),
            "start", now - 3_600_000,
            "end", now
        )
        val response = pyroscope.post("/querier.v1.QuerierService/LabelValues")
            .contentType(MediaTypes.APPLICATION_JSON)
            .submit(request.toJSONString())
        assertThat(response.status()).isEqualTo(Status.OK_200)
        // Pyroscope omits the field when there are no values.
        val names = JSON.parseObject(response.`as`(String::class.java)).getJSONArray("names") ?: return emptyList()
        return names.toList(String::class.java)
    }

    private fun awaitServerSpan(traceId: String): JSONObject {
        val tempo = WebClient.builder()
            .baseUri(System.getProperty(LgtmContainerRunner.TEMPO_URL_PROPERTY))
            .build()
        val deadline = System.currentTimeMillis() + 30_000
        while (System.currentTimeMillis() < deadline) {
            val response = tempo.get("/api/v2/traces/$traceId").request()
            if (response.status() == Status.OK_200) {
                val server = serverSpan(response.`as`(String::class.java))
                if (server != null) {
                    return server
                }
            } else {
                response.close()
            }
            Thread.sleep(500)
        }
        throw AssertionError("Trace $traceId lacks a server span in Tempo")
    }

    private fun serverSpan(body: String): JSONObject? {
        // Tempo answers 200 with an empty trace until it's ingested.
        val resourceSpans = JSON.parseObject(body).getJSONObject("trace").getJSONArray("resourceSpans") ?: return null
        for (resourceSpan in resourceSpans.toList(JSONObject::class.java)) {
            for (scopeSpan in resourceSpan.getJSONArray("scopeSpans").toList(JSONObject::class.java)) {
                for (span in scopeSpan.getJSONArray("spans").toList(JSONObject::class.java)) {
                    if (span.getString("kind") == "SPAN_KIND_SERVER") {
                        return span
                    }
                }
            }
        }
        return null
    }

    private fun attribute(span: JSONObject, key: String): String? {
        for (attribute in span.getJSONArray("attributes").toList(JSONObject::class.java)) {
            if (attribute.getString("key") == key) {
                return attribute.getJSONObject("value").getString("stringValue")
            }
        }
        return null
    }

    private companion object {
        const val CPU_PROFILE_TYPE = "process_cpu:cpu:nanoseconds:cpu:nanoseconds"
        val TRACEPARENT = HeaderNames.create("traceparent")
    }
}
