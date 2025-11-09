package common

import io.helidon.webclient.api.WebClient

object Common {

    @JvmStatic
    fun helidonClient(
        host: String,
        port: Int
    ): WebClient {
        val client = WebClient.builder()
            .baseUri("http://${host}:${port}")
            .build()
        return client
    }

}