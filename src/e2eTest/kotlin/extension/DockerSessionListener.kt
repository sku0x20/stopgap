package extension

import org.junit.platform.launcher.LauncherSession
import org.junit.platform.launcher.LauncherSessionListener


class DockerSessionListener : LauncherSessionListener {

    override fun launcherSessionOpened(session: LauncherSession) {
        System.err.println("session opened")
    }

    override fun launcherSessionClosed(session: LauncherSession) {
        System.err.println("session closed")

    }
}