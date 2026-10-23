package dev.sku20.stopgap.app.webclient

import io.helidon.webclient.api.WebClientServiceRequest
import io.helidon.webclient.api.WebClientServiceResponse
import io.helidon.webclient.spi.WebClientService
import org.slf4j.Logger
import org.slf4j.LoggerFactory

// Duration runs until the response headers arrive, same as the client span; body reads aren't included.
class LoggingClientService : WebClientService {

    override fun handle(chain: WebClientService.Chain, request: WebClientServiceRequest): WebClientServiceResponse {
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
            logger.warn("{} {} failed in {} ms", request.method().text(), target(request), elapsedMillis(startNanos), e)
            throw e
        }
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
