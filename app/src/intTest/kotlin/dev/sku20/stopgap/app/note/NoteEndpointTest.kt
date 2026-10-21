package dev.sku20.stopgap.app.note

import dev.sku20.stopgap.app.serde.fastjson.FastjsonSerde
import dev.sku20.stopgap.helidon.serde.MapSerdeCatalog
import dev.sku20.stopgap.helidon.test.InjectInstance
import dev.sku20.stopgap.helidon.test.integration.SetupCapture
import dev.sku20.stopgap.helidon.test.integration.WebserverTest
import io.helidon.http.HttpMediaTypes
import io.helidon.http.Status
import io.helidon.webclient.api.WebClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import kotlin.uuid.Uuid

@WebserverTest
class NoteEndpointTest {

    @InjectInstance
    lateinit var client: WebClient

    @InjectInstance
    lateinit var noteStore: NoteStore

    // Reset is usually a smell, but the server, and so this mock, is shared by every test in the class: without it,
    // stubs and recorded calls leak between tests.
    @BeforeEach
    fun resetStore() {
        reset(noteStore)
    }

    @Test
    fun create() {
        val note = Note(Uuid.random(), "foo")
        whenever(noteStore.create(NoteReq("foo"))).thenReturn(note)

        val response = client.post("/notes")
            .contentType(HttpMediaTypes.JSON_UTF_8)
            .accept(HttpMediaTypes.JSON_UTF_8)
            .submit("""{"text":"foo"}""")
        assertThat(response.status()).isEqualTo(Status.OK_200)
        assertThat(response.`as`(String::class.java)).isEqualTo(json(note))
    }

    @Test
    fun get() {
        val note = Note(Uuid.random(), "foo")
        whenever(noteStore.get(note.noteId)).thenReturn(note)

        val response = client.get("/notes/${note.noteId}")
            .accept(HttpMediaTypes.JSON_UTF_8)
            .request()
        assertThat(response.status()).isEqualTo(Status.OK_200)
        assertThat(response.`as`(String::class.java)).isEqualTo(json(note))
    }

    @Test
    fun list() {
        val first = Note(Uuid.random(), "foo")
        val second = Note(Uuid.random(), "bar")
        whenever(noteStore.list()).thenReturn(listOf(first, second))

        val response = client.get("/notes")
            .accept(HttpMediaTypes.JSON_UTF_8)
            .request()
        assertThat(response.status()).isEqualTo(Status.OK_200)
        assertThat(response.`as`(String::class.java)).isEqualTo("[${json(first)},${json(second)}]")
    }

    @Test
    fun update() {
        val note = Note(Uuid.random(), "bar")
        whenever(noteStore.update(UpdateReq(note.noteId, "bar"))).thenReturn(note)

        val response = client.patch("/notes/${note.noteId}")
            .contentType(HttpMediaTypes.JSON_UTF_8)
            .accept(HttpMediaTypes.JSON_UTF_8)
            .submit("""{"text":"bar"}""")
        assertThat(response.status()).isEqualTo(Status.OK_200)
        assertThat(response.`as`(String::class.java)).isEqualTo(json(note))
    }

    @Test
    fun delete() {
        val noteId = Uuid.random()

        val response = client.delete("/notes/$noteId").accept(HttpMediaTypes.JSON_UTF_8).request()
        assertThat(response.status()).isEqualTo(Status.OK_200)
        verify(noteStore).delete(noteId)
    }

    @Test
    fun getMissing() {
        val noteId = Uuid.random()
        whenever(noteStore.get(noteId)).thenThrow(NoteNotFoundException(noteId))

        val response = client.get("/notes/$noteId").accept(HttpMediaTypes.JSON_UTF_8).request()
        assertThat(response.status()).isEqualTo(Status.NOT_FOUND_404)
    }

    @Test
    fun updateMissing() {
        val noteId = Uuid.random()
        whenever(noteStore.update(UpdateReq(noteId, "bar"))).thenThrow(NoteNotFoundException(noteId))

        val response = client.patch("/notes/$noteId")
            .contentType(HttpMediaTypes.JSON_UTF_8)
            .accept(HttpMediaTypes.JSON_UTF_8)
            .submit("""{"text":"bar"}""")
        assertThat(response.status()).isEqualTo(Status.NOT_FOUND_404)
    }

    @Test
    fun deleteMissing() {
        val noteId = Uuid.random()
        doThrow(NoteNotFoundException(noteId)).whenever(noteStore).delete(noteId)

        val response = client.delete("/notes/$noteId").accept(HttpMediaTypes.JSON_UTF_8).request()
        assertThat(response.status()).isEqualTo(Status.NOT_FOUND_404)
    }

    @Test
    fun invalidId() {
        val response = client.get("/notes/abc").accept(HttpMediaTypes.JSON_UTF_8).request()
        assertThat(response.status()).isEqualTo(Status.BAD_REQUEST_400)
        verifyNoInteractions(noteStore)
    }

    private fun json(note: Note): String = """{"noteId":"${note.noteId}","text":"${note.text}"}"""

    companion object {
        @JvmStatic
        @WebserverTest.Setup
        fun setup(): SetupCapture {
            val catalog = MapSerdeCatalog()
            catalog.add(FastjsonSerde())
            val noteStore = mock<NoteStore>()
            return SetupCapture(
                NoteEndpoint(noteStore),
                registerParams = arrayOf(catalog),
                instances = mapOf(NoteStore::class.java to noteStore)
            )
        }
    }
}
