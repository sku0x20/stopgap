package extension

import org.junit.platform.launcher.LauncherSession
import org.junit.platform.launcher.LauncherSessionListener
import org.testcontainers.containers.GenericContainer


class DockerImageRunner : LauncherSessionListener {

    companion object {
        private const val DOCKER_IMAGE_NAME = "stopgap:e2e"
    }

    private val container = GenericContainer(DOCKER_IMAGE_NAME)
        .withExposedPorts(8080)

    override fun launcherSessionOpened(session: LauncherSession) {
        container.start()
        System.setProperty("container.server.port", container.getMappedPort(8080).toString())
        System.err.println("container started: ${container.containerId}")
    }

    override fun launcherSessionClosed(session: LauncherSession) {
        container.stop()
        System.err.println("container stopped")

    }
}