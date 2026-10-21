package dev.sku20.stopgap.app.note

import dev.sku20.stopgap.helidon.endpoint.*
import dev.sku20.stopgap.helidon.param.PathParam
import kotlin.uuid.Uuid

// TODO: explore ETag;
@Endpoint("/notes")
class NoteEndpoint(
    private val noteStore: NoteStore
) {

    @Post("/")
    fun create(body: NoteReq): Note {
        return noteStore.create(body)
    }

    @Get("/")
    fun list(): List<Note> {
        return noteStore.list()
    }

    @Get("/{noteId}")
    fun get(
        @PathParam("noteId")
        noteId: String
    ): Note {
        return noteStore.get(parseId(noteId))
    }

    @Patch("/{noteId}")
    fun update(
        @PathParam("noteId")
        noteId: String,
        body: NoteReq
    ): Note {
        return noteStore.update(UpdateReq(parseId(noteId), body.text))
    }

    @Delete("/{noteId}")
    fun delete(
        @PathParam("noteId")
        noteId: String
    ) {
        noteStore.delete(parseId(noteId))
    }

    private fun parseId(noteId: String): Uuid =
        Uuid.parseOrNull(noteId) ?: throw InvalidNoteIdException(noteId)

}
