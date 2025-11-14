package extension

import org.junit.platform.launcher.LauncherSession
import org.junit.platform.launcher.LauncherSessionListener
import org.testcontainers.containers.GenericContainer


class DockerSessionListener : LauncherSessionListener {

    companion object {
        private const val DOCKER_IMAGE_NAME = "stopgap:e2e"
    }

    private val container = GenericContainer(DOCKER_IMAGE_NAME)

    override fun launcherSessionOpened(session: LauncherSession) {
        container.start()
        System.err.println("container started: ${container.containerId}")
    }

    override fun launcherSessionClosed(session: LauncherSession) {
        container.stop()
        System.err.println("container stopped")

    }
}