package dev.sku20.stopgap.helidon.test.client

import java.util.ServiceLoader

class Clients {

    private val clients = mutableMapOf<Class<*>, ClientHolder>()

    @Suppress("UNCHECKED_CAST")
    fun setup(host: String, port: Int) {
        for (webserverClient in ServiceLoader.load(ClientProvider::class.java) as ServiceLoader<ClientProvider<Any>>) {
            val client = webserverClient.create(host, port)
            val holder = ClientHolder(webserverClient, client)
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
