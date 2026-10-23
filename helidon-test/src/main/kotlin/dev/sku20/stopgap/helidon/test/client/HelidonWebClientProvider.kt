package dev.sku20.stopgap.helidon.test.client

import io.helidon.webclient.api.WebClient

class HelidonWebClientProvider : WebClientProvider<WebClient> {

    override fun create(host: String, port: Int): WebClient {
        // No discovery: client services on the classpath (tracing, metrics) would otherwise join every test call.
        val client = WebClient.builder()
            .baseUri("http://${host}:${port}")
            .servicesDiscoverServices(false)
            .build()
        return client
    }

    override fun close(client: WebClient) {
        client.closeResource()
    }
}
