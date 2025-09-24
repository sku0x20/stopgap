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

    fun <T> registerForType(
        clazz: Class<T>,
        creator: InstanceCreator<T>
    ) {
        registerForQualifier(clazz.getName(), creator)
    }

    fun registerForQualifier(
        qualifier: String,
        creator: InstanceCreator<*>
    ) {
        if (creators.containsKey(qualifier)) throw CreatorExistsException(qualifier)
        creators[qualifier] = creator
    }

    fun <T> getInstanceForType(
        clazz: Class<T>
    ): T {
        return getInstanceForQualifier<T>(clazz.getName(), clazz)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getInstanceForQualifier(
        qualifier: String,
        clazz: Class<T>
    ): T {
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
