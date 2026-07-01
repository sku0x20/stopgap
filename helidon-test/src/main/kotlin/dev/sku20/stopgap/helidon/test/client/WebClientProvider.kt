package dev.sku20.stopgap.helidon.test.client

/**
 * Represents an HTTP client for testing against a running webserver.
 *
 * Implementations are discovered via [java.util.ServiceLoader]. To register a custom client,
 * create `META-INF/services/dev.sku20.stopgap.helidon.test.client.WebClientProvider` in your
 * test resources and list the fully qualified class name of your implementation.
 */
interface WebClientProvider<T> {
    fun create(host: String, port: Int): T
    fun close(client: T)
}
