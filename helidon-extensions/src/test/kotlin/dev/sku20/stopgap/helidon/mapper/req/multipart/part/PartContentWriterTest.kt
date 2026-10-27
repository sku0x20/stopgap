package dev.sku20.stopgap.helidon.mapper.req.multipart.part

import dev.sku20.stopgap.helidon.mapper.req.multipart.part.partcontent.FilePartContent
import dev.sku20.stopgap.helidon.mapper.req.multipart.part.partcontent.InMemoryPartContent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PartContentWriterTest {

    @Test
    fun keepsUpToBufferSizeInMemory() {
        val writer = PartContentWriter(4)

        write(writer, "abcd")
        val content = writer.content()

        assertThat(content).isInstanceOf(InMemoryPartContent::class.java)
        assertThat(content.inputStream().readAllBytes()).isEqualTo("abcd".toByteArray())
    }

    @Test
    fun movesOverBufferSizeToFile() {
        val writer = PartContentWriter(4)

        write(writer, "abcdefghij")
        val content = writer.content()

        assertThat(content).isInstanceOf(FilePartContent::class.java)
        assertThat(content.inputStream().readAllBytes()).isEqualTo("abcdefghij".toByteArray())
        content.close()
    }

    private fun write(writer: PartContentWriter, value: String) {
        for (byte in value.toByteArray()) {
            writer.write(byte.toInt())
        }
    }
}
