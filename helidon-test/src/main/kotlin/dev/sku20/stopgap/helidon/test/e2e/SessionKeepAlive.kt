package dev.sku20.stopgap.helidon.test.e2e

import org.junit.platform.launcher.LauncherSession
import org.junit.platform.launcher.LauncherSessionListener

// Register last: JUnit closes session listeners in reverse order, so blocking here keeps every container up.
// Ctrl-C kills the test JVM and Ryuk removes the containers. An env var because the forked test JVM inherits
// the shell's environment but not Gradle's -D properties.
class SessionKeepAlive : LauncherSessionListener {

    override fun launcherSessionClosed(session: LauncherSession) {
        if (!System.getenv("E2E_KEEP_ALIVE").toBoolean()) return
        val port = session.store.get(E2eStore.NAMESPACE, E2eStoreKeys.PORT)
        System.err.println("e2e finished; containers kept running (app: http://localhost:$port). Ctrl-C to stop.")
        Thread.sleep(Long.MAX_VALUE)
    }
}
