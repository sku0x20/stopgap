package dev.sku20.stopgap.helidon.test.e2e

import org.junit.platform.launcher.LauncherSession
import org.junit.platform.launcher.LauncherSessionListener
import java.util.Properties

class TestPropertiesLoader : LauncherSessionListener {

    override fun launcherSessionOpened(session: LauncherSession) {
        val store = session.store
        loadProperties().forEach { key, value ->
            store.put(E2eStore.NAMESPACE, key, value)
        }
    }

    private fun loadProperties(): Properties {
        val stream = TestPropertiesLoader::class.java.classLoader.getResourceAsStream("stopgap-test.properties")
            ?: error("stopgap-test.properties not found on classpath")
        return Properties().also { it.load(stream) }
    }
}
