package extension.webserverclient

class ClientHolder(
    val webserverClient: WebserverClient<Any>,
    val client: Any,
) {

    val type = client::class.java

    fun close() {
        webserverClient.close(client)
    }
}