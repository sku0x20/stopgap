package dev.sku20.stopgap.helidon.test.client

class ClientProviderHolder(
    val webserverClient: WebClientProvider<Any>,
    val client: Any,
) {

    val type = client::class.java

    fun close() {
        webserverClient.close(client)
    }
}
