package dev.sku20.stopgap.helidon.test.e2e

import org.junit.platform.launcher.LauncherSession
import org.junit.platform.launcher.LauncherSessionListener
import java.util.Properties

class DockerConfig : LauncherSessionListener {

    override fun launcherSessionOpened(session: LauncherSession) {
        val props = loadProperties()
        val store = session.store
        store.put(E2eStore.NAMESPACE, E2eStoreKeys.DOCKER_IMAGE, props.require(E2eStoreKeys.DOCKER_IMAGE))
        store.put(E2eStore.NAMESPACE, E2eStoreKeys.SERVICE_NAME, props.require(E2eStoreKeys.SERVICE_NAME))
        props.getProperty(E2eStoreKeys.ENV_FILE)?.let {
            store.put(E2eStore.NAMESPACE, E2eStoreKeys.ENV_FILE, it)
        }
    }

    private fun loadProperties(): Properties {
        val stream = DockerConfig::class.java.classLoader.getResourceAsStream("stopgap-test.properties")
            ?: error("stopgap-test.properties not found on classpath")
        return Properties().also { it.load(stream) }
    }

    private fun Properties.require(key: String): String =
        getProperty(key) ?: error("Missing required property '$key' in stopgap-test.properties")
}
