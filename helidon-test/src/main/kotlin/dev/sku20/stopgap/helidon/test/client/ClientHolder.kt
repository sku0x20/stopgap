package dev.sku20.stopgap.helidon.test.client

class ClientHolder(
    val webserverClient: ClientProvider<Any>,
    val client: Any,
) {

    val type = client::class.java

    fun close() {
        webserverClient.close(client)
    }
}
