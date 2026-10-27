package dev.sku20.stopgap.helidon.mapper.req.multipart.part.partcontent

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class InMemoryPartContentTest {

    @Test
    fun exposesSizeAndBytes() {
        val content = InMemoryPartContent("hello".toByteArray())

        assertThat(content.size).isEqualTo(5)
        assertThat(content.inputStream().readAllBytes()).isEqualTo("hello".toByteArray())
    }

    @Test
    fun rejectsMoreThanMaxSize() {
        assertThatThrownBy { InMemoryPartContent(ByteArray(InMemoryPartContent.MAX_SIZE + 1)) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }
}
