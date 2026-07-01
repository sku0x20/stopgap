package dev.sku20.stopgap.helidon.test.client

/**
 * Factory for creating and closing a web client for testing against a running webserver.
 *
 * Implementations are discovered via [java.util.ServiceLoader]. To register a custom provider,
 * create `META-INF/services/dev.sku20.stopgap.helidon.test.client.WebClientProvider` in your
 * test resources and list the fully qualified class name of your implementation.
 *
 * [HelidonWebClientProvider] is registered by default.
 */
interface WebClientProvider<T> {
    fun create(host: String, port: Int): T
    fun close(client: T)
}
