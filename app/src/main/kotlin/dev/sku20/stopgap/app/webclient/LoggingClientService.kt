package dev.sku20.stopgap.app.webclient

import io.helidon.webclient.api.WebClientServiceRequest
import io.helidon.webclient.api.WebClientServiceResponse
import io.helidon.webclient.spi.WebClientService
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanContext
import io.opentelemetry.context.Scope
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import io.helidon.tracing.SpanContext as HelidonSpanContext

// Duration runs until the response headers arrive, same as the client span; body reads aren't included.
class LoggingClientService : WebClientService {

    override fun handle(chain: WebClientService.Chain, request: WebClientServiceRequest): WebClientServiceResponse {
        activateClientSpan(request).use {
            val startNanos = System.nanoTime()
            try {
                val response = chain.proceed(request)
                logger.info(
                    "{} {} -> {} in {} ms",
                    request.method().text(),
                    target(request),
                    response.status().code(),
                    elapsedMillis(startNanos)
                )
                return response
            } catch (e: RuntimeException) {
                logger.warn(
                    "{} {} failed in {} ms",
                    request.method().text(),
                    target(request),
                    elapsedMillis(startNanos),
                    e
                )
                throw e
            }
        }
    }

    /**
     * Makes the client span current in OTel so this service's log line carries its span id.
     *
     * `WebClientTracing` keeps its span only in Helidon's request `Context` (as a `SpanContext`) and never activates
     * it in OTel. The Logback appender reads OTel's `Context.current()`, so without this the line carries the server
     * span's id and "logs for this span" on the client span finds nothing; only the trace id matches.
     *
     * This is the workaround from helidon#8861: a span must be in Helidon's `Context` *and* current in OTel for every
     * consumer to see it. Helidon's `SpanContext` exposes only ids, so the span is rebuilt with `Span.wrap`; flags and
     * state come from the current (server) span, since both share one trace and its sampling decision.
     *
     * Helidon chose not to trust the thread's current span for WebClient: in 2.x the call could run on another
     * thread, so "current" might belong to another request (helidon#5216, reopened for 4.x where virtual threads
     * make it safe). The newer `helidon-webclient-telemetry-tracing` has the same gap.
     *
     * - [helidon#8861](https://github.com/helidon-io/helidon/issues/8861)
     * - [helidon#5216](https://github.com/helidon-io/helidon/issues/5216)
     */
    private fun activateClientSpan(request: WebClientServiceRequest): Scope {
        val clientSpan = request.context().get(HelidonSpanContext::class.java)
        if (clientSpan.isEmpty) {
            return Scope.noop()
        }
        val parent = Span.current().spanContext
        val spanContext = SpanContext.create(
            clientSpan.get().traceId(),
            clientSpan.get().spanId(),
            parent.traceFlags,
            parent.traceState
        )
        return Span.wrap(spanContext).makeCurrent()
    }

    // Query left out: it may carry secrets.
    private fun target(request: WebClientServiceRequest): String {
        val uri = request.uri()
        return "${uri.scheme()}://${uri.authority()}${uri.path().path()}"
    }

    private fun elapsedMillis(startNanos: Long): Long =
        (System.nanoTime() - startNanos) / 1_000_000

    private companion object {
        val logger: Logger = LoggerFactory.getLogger(LoggingClientService::class.java)
    }
}
