package dev.sku20.stopgap.helidon.test.client

import org.junit.platform.commons.support.ModifierSupport
import org.junit.platform.commons.support.ReflectionSupport

class Clients {

    private val clients = mutableMapOf<Class<*>, ClientHolder>()

    @Suppress("UNCHECKED_CAST")
    fun setup(host: String, port: Int) {
        for (clazz in classes) {
            val webserverClient = clazz.getConstructor().newInstance() as WebserverClient<Any>
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

    companion object {
        private val classes by lazy { findClasses() }

        @Suppress("UNCHECKED_CAST")
        private fun findClasses(): List<Class<out WebserverClient<*>>> {
            val roots = this::class.java.classLoader.getResources("").toList()
            return roots.flatMap { root ->
                ReflectionSupport.findAllClassesInClasspathRoot(root.toURI(), { clazz ->
                    WebserverClient::class.java.isAssignableFrom(clazz)
                        && !clazz.isInterface
                        && ModifierSupport.isPublic(clazz)
                }, { true }) as List<Class<out WebserverClient<*>>>
            }
        }
    }
}
