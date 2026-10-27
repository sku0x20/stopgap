package dev.sku20.stopgap.helidon.mapper.req.multipart.part.partcontent

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class FilePartContentTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun exposesSizeAndBytes() {
        val file = Files.write(tempDir.resolve("part"), "hello".toByteArray())

        val content = FilePartContent(file)

        assertThat(content.size).isEqualTo(5)
        assertThat(content.inputStream().readAllBytes()).isEqualTo("hello".toByteArray())
    }

    @Test
    fun closeDeletesFile() {
        val file = Files.write(tempDir.resolve("part"), "hello".toByteArray())

        FilePartContent(file).close()

        assertThat(file).doesNotExist()
    }

    @Test
    fun writeAppends() {
        val content = FilePartContent(Files.createFile(tempDir.resolve("part")))

        content.startWriting()
        content.write("hel".toByteArray())
        content.write("lo".toByteArray())
        content.finishWriting()

        assertThat(content.size).isEqualTo(5)
        assertThat(content.inputStream().readAllBytes()).isEqualTo("hello".toByteArray())
    }

    @Test
    fun rejectsCallsOutOfOrder() {
        val content = FilePartContent(Files.createFile(tempDir.resolve("part")))

        assertThatThrownBy { content.write("a".toByteArray()) }
            .isInstanceOf(IllegalStateException::class.java)
        assertThatThrownBy { content.finishWriting() }
            .isInstanceOf(IllegalStateException::class.java)

        content.startWriting()

        assertThatThrownBy { content.inputStream() }
            .isInstanceOf(IllegalStateException::class.java)
        assertThatThrownBy { content.size }
            .isInstanceOf(IllegalStateException::class.java)
    }
}
