package dev.sku20.stopgap.helidon.test.clients

import dev.sku20.stopgap.helidon.test.extension.webserverclient.WebserverClient
import io.helidon.webclient.api.WebClient

class HelidonWebClient : WebserverClient<WebClient> {

    override fun create(host: String, port: Int): WebClient {
        val client = WebClient.builder()
            .baseUri("http://${host}:${port}")
            .build()
        return client
    }

    override fun close(client: WebClient) {
        client.closeResource()
    }
}
