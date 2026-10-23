package dev.sku20.stopgap.app.note.store

import dev.sku20.stopgap.app.note.Note
import dev.sku20.stopgap.app.note.NoteReq
import dev.sku20.stopgap.app.note.UpdateReq
import kotlin.uuid.Uuid

interface NoteStore {

    fun create(req: NoteReq): Note

    fun list(): List<Note>

    fun get(noteId: Uuid): Note

    fun update(req: UpdateReq): Note

    fun delete(noteId: Uuid)

}
