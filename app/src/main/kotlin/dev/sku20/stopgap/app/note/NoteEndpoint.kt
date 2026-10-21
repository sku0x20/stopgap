package dev.sku20.stopgap.app.note

import dev.sku20.stopgap.helidon.endpoint.Delete
import dev.sku20.stopgap.helidon.endpoint.Endpoint
import dev.sku20.stopgap.helidon.endpoint.Get
import dev.sku20.stopgap.helidon.endpoint.Patch
import dev.sku20.stopgap.helidon.endpoint.Post
import dev.sku20.stopgap.helidon.param.PathParam
import java.util.concurrent.ConcurrentHashMap
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Endpoint("/notes")
class NoteEndpoint {

    // in-memory until postgres is wired
    private val notes = ConcurrentHashMap<Uuid, Note>()

    @OptIn(ExperimentalUuidApi::class)
    @Post("/")
    fun create(body: NoteReq): Note {
        val note = Note(Uuid.generateV7(), body.text)
        notes[note.noteId] = note
        return note
    }

    // uuid7 sorts by creation time (ms precision)
    @Get("/")
    fun list(): List<Note> = notes.values.sortedBy { it.noteId }

    @Get("/{noteId}")
    fun get(@PathParam("noteId") noteId: String): Note = find(parseId(noteId))

    @Patch("/{noteId}")
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
        notes.remove(id) ?: throw NoteNotFoundException(id)
    }

    private fun find(id: Uuid): Note = notes[id] ?: throw NoteNotFoundException(id)

    private fun parseId(noteId: String): Uuid = Uuid.parseOrNull(noteId) ?: throw InvalidNoteIdException(noteId)

}
