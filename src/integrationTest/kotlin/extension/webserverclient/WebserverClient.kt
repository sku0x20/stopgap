package extension.webserverclient

interface WebserverClient<T> {
    fun create(host: String, port: Int): T
    fun stop(client: T)
}