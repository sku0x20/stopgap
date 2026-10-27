package dev.sku20.stopgap.helidon.mapper.req.multipart.part.partcontent

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class TempFilePartContentTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun exposesSizeAndBytes() {
        val file = Files.write(tempDir.resolve("part"), "hello".toByteArray())

        val content = TempFilePartContent(file)

        assertThat(content.size).isEqualTo(5)
        assertThat(content.inputStream().readAllBytes()).isEqualTo("hello".toByteArray())
    }

    @Test
    fun closeDeletesFile() {
        val file = Files.write(tempDir.resolve("part"), "hello".toByteArray())

        TempFilePartContent(file).close()

        assertThat(file).doesNotExist()
    }

    @Test
    fun writeAppends() {
        val content = TempFilePartContent(Files.createFile(tempDir.resolve("part")))

        content.write("hel".toByteArray())
        content.write("lo".toByteArray())

        assertThat(content.size).isEqualTo(5)
        assertThat(content.inputStream().readAllBytes()).isEqualTo("hello".toByteArray())
    }
}
