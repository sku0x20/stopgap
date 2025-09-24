package com.example.stopgap.instanceregistry

/**
 * A registry for managing instance creation and retrieval. This class allows for registering
 * instance creators by type or qualifier and retrieving instances as needed. Instances are
 * instantiated lazily and cached for subsequent access.
 * 
 * 
 * Thread-safety is not guaranteed, and it is up to the caller to ensure appropriate usage in
 * multithreaded environments.
 */
class InstanceRegistry(
    val config: Config
) {

    private val instances: MutableMap<String, Any> = HashMap()
    private val creators: MutableMap<String, InstanceCreator<*>> = HashMap()

    inline fun <reified T> registerForType(
        creator: InstanceCreator<T>
    ) {
        registerForQualifier(T::class.qualifiedName!!, creator)
    }

    inline fun <reified T> getInstanceForType(): T {
        return getInstanceForQualifier(T::class.qualifiedName!!)
    }

    fun registerForQualifier(
        qualifier: String,
        creator: InstanceCreator<*>
    ) {
        if (creators.containsKey(qualifier)) throw CreatorExistsException(qualifier)
        creators[qualifier] = creator
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getInstanceForQualifier(qualifier: String): T {
        val instance = instances[qualifier] ?: createInstance(qualifier)
        return instance as T
    }

    private fun createInstance(qualifier: String): Any {
        val creator = creators[qualifier] ?: throw NoCreatorException(qualifier)
        val instance = creator.create(this)!!
        instances[qualifier] = instance
        return instance
    }
}
