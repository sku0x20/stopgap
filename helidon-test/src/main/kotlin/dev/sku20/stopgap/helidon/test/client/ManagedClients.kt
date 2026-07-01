package dev.sku20.stopgap.helidon.test.client

import java.util.*

class ManagedClients {

    private val clients = mutableMapOf<Class<*>, ClientProviderHolder>()

    @Suppress("UNCHECKED_CAST")
    fun setup(host: String, port: Int) {
        val providers = ServiceLoader.load(WebClientProvider::class.java) as ServiceLoader<WebClientProvider<Any>>
        for (webserverClient in providers) {
            val client = webserverClient.create(host, port)
            val holder = ClientProviderHolder(webserverClient, client)
            clients[holder.type] = holder
        }
    }

    fun findClient(clazz: Class<*>): Any? {
        val holder = clients[clazz]
        if (holder != null) return holder.client
        for (key in clients.keys) {
            if (clazz.isAssignableFrom(key)) {
                return clients[key]!!.client
            }
        }
        return null
    }

    fun closeClients() {
        for (clientHolder in clients.values) {
            clientHolder.close()
        }
    }
}
