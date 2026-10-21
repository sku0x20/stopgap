package dev.sku20.stopgap.app.note

import com.alibaba.fastjson2.JSON
import dev.sku20.stopgap.app.serde.fastjson.FastjsonSerde
import dev.sku20.stopgap.helidon.serde.MapSerdeCatalog
import dev.sku20.stopgap.helidon.test.InjectInstance
import dev.sku20.stopgap.helidon.test.integration.SetupCapture
import dev.sku20.stopgap.helidon.test.integration.WebserverTest
import io.helidon.http.HttpMediaTypes
import io.helidon.http.Status
import io.helidon.webclient.api.WebClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

@WebserverTest
class NoteEndpointTest {

    @InjectInstance
    lateinit var client: WebClient

    @Test
    fun create() {
        val response = client.post("/notes")
            .contentType(HttpMediaTypes.JSON_UTF_8)
            .accept(HttpMediaTypes.JSON_UTF_8)
            .submit("""{"text":"foo"}""")
        assertThat(response.status()).isEqualTo(Status.OK_200)
        val note = JSON.parseObject(response.`as`(String::class.java), Note::class.java)
        assertThat(note.text).isEqualTo("foo")
    }

    @Test
    fun get() {
        val created = create("foo")

        val response = client.get("/notes/${created.noteId}")
            .accept(HttpMediaTypes.JSON_UTF_8)
            .request()
        assertThat(response.status()).isEqualTo(Status.OK_200)
        assertThat(response.`as`(String::class.java))
            .isEqualTo("""{"noteId":"${created.noteId}","text":"foo"}""")
    }

    @Test
    fun list() {
        val first = create("foo")
        val second = create("bar")

        val response = client.get("/notes")
            .accept(HttpMediaTypes.JSON_UTF_8)
            .request()
        assertThat(response.status()).isEqualTo(Status.OK_200)
        val notes = JSON.parseArray(response.`as`(String::class.java), Note::class.java)
        assertThat(notes).contains(first, second)
    }

    @Test
    fun update() {
        val created = create("foo")

        val response = client.put("/notes/${created.noteId}")
            .contentType(HttpMediaTypes.JSON_UTF_8)
            .accept(HttpMediaTypes.JSON_UTF_8)
            .submit("""{"text":"bar"}""")
        assertThat(response.status()).isEqualTo(Status.OK_200)
        assertThat(get(created.noteId).text).isEqualTo("bar")
    }

    @Test
    fun delete() {
        val created = create("foo")

        val response = client.delete("/notes/${created.noteId}").accept(HttpMediaTypes.JSON_UTF_8).request()
        assertThat(response.status()).isEqualTo(Status.OK_200)
        assertThat(client.get("/notes/${created.noteId}").accept(HttpMediaTypes.JSON_UTF_8).request().status()).isEqualTo(Status.NOT_FOUND_404)
    }

    @Test
    fun getMissing() {
        val response = client.get("/notes/${UUID.randomUUID()}").accept(HttpMediaTypes.JSON_UTF_8).request()
        assertThat(response.status()).isEqualTo(Status.NOT_FOUND_404)
    }

    @Test
    fun updateMissing() {
        val response = client.put("/notes/${UUID.randomUUID()}")
            .contentType(HttpMediaTypes.JSON_UTF_8)
            .accept(HttpMediaTypes.JSON_UTF_8)
            .submit("""{"text":"bar"}""")
        assertThat(response.status()).isEqualTo(Status.NOT_FOUND_404)
    }

    @Test
    fun deleteMissing() {
        val response = client.delete("/notes/${UUID.randomUUID()}").accept(HttpMediaTypes.JSON_UTF_8).request()
        assertThat(response.status()).isEqualTo(Status.NOT_FOUND_404)
    }

    @Test
    fun invalidId() {
        val response = client.get("/notes/abc").accept(HttpMediaTypes.JSON_UTF_8).request()
        assertThat(response.status()).isEqualTo(Status.BAD_REQUEST_400)
    }

    private fun create(text: String): Note {
        val response = client.post("/notes")
            .contentType(HttpMediaTypes.JSON_UTF_8)
            .accept(HttpMediaTypes.JSON_UTF_8)
            .submit("""{"text":"$text"}""")
        return JSON.parseObject(response.`as`(String::class.java), Note::class.java)
    }

    private fun get(noteId: UUID): Note {
        val response = client.get("/notes/$noteId")
            .accept(HttpMediaTypes.JSON_UTF_8)
            .request()
        return JSON.parseObject(response.`as`(String::class.java), Note::class.java)
    }

    companion object {
        @JvmStatic
        @WebserverTest.Setup
        fun setup(): SetupCapture {
            val catalog = MapSerdeCatalog()
            catalog.add(FastjsonSerde())
            return SetupCapture(NoteEndpoint(), registerParams = arrayOf(catalog))
        }
    }
}
