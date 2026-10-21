package dev.sku20.stopgap.app.note

import dev.sku20.stopgap.helidon.endpoint.Delete
import dev.sku20.stopgap.helidon.endpoint.Endpoint
import dev.sku20.stopgap.helidon.endpoint.Get
import dev.sku20.stopgap.helidon.endpoint.Post
import dev.sku20.stopgap.helidon.endpoint.Put
import dev.sku20.stopgap.helidon.param.PathParam
import io.helidon.http.HttpException
import io.helidon.http.Status
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

@Endpoint("/notes")
class NoteEndpoint {

    // in-memory until postgres is wired
    private val notes = ConcurrentHashMap<UUID, Note>()

    @OptIn(ExperimentalUuidApi::class)
    @Post("/")
    fun create(body: NoteReq): Note {
        val note = Note(Uuid.generateV7().toJavaUuid(), body.text)
        notes[note.noteId] = note
        return note
    }

    @Get("/")
    fun list(): List<Note> {
        // uuid7 sorts by creation time (ms precision)
        return notes.values.sortedBy { it.noteId }
    }

    @Get("/{noteId}")
    fun get(@PathParam("noteId") noteId: String): Note {
        return find(parseId(noteId))
    }

    @Put("/{noteId}")
    fun update(@PathParam("noteId") noteId: String, body: NoteReq): Note {
        val id = parseId(noteId)
        find(id)
        val note = Note(id, body.text)
        notes[id] = note
        return note
    }

    @Delete("/{noteId}")
    fun delete(@PathParam("noteId") noteId: String) {
        val id = parseId(noteId)
        if (notes.remove(id) == null) {
            throw notFound(id)
        }
    }

    private fun find(id: UUID): Note {
        val note = notes[id]
        if (note == null) {
            throw notFound(id)
        }
        return note
    }

    private fun parseId(noteId: String): UUID {
        val id = Uuid.parseOrNull(noteId)
        if (id == null) {
            throw HttpException("Invalid note id: $noteId", Status.BAD_REQUEST_400)
        }
        return id.toJavaUuid()
    }

    private fun notFound(id: UUID): HttpException {
        return HttpException("Note not found: $id", Status.NOT_FOUND_404)
    }

}
