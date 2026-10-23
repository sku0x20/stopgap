package dev.sku20.stopgap.gradle

import org.gradle.api.GradleException
import org.gradle.api.provider.ValueSource
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import javax.inject.Inject

/**
 * The short commit of HEAD, or `unknown` when git is missing (e.g. inside an image build), [GitShaParameters.dir]
 * isn't a repository, or HEAD has no commit yet.
 */
// Abstract only because Gradle generates getParameters(); providers.exec has no way to catch a missing git.
abstract class GitShaSource @Inject constructor(
    private val execOperations: ExecOperations
) : ValueSource<String, GitShaParameters> {

    override fun obtain(): String {
        val output = ByteArrayOutputStream()
        try {
            execOperations.exec {
                it.commandLine("git", "-C", parameters.dir.get().asFile.absolutePath, "rev-parse", "--short", "HEAD")
                it.standardOutput = output
                it.errorOutput = ByteArrayOutputStream()
            }
        } catch (e: GradleException) {
            return "unknown"
        }
        return output.toString().trim()
    }
}
