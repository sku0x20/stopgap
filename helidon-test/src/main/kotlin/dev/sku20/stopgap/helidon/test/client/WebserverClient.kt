package dev.sku20.stopgap.helidon.test.client

interface WebserverClient<T> {
    fun create(host: String, port: Int): T
    fun close(client: T)
}
