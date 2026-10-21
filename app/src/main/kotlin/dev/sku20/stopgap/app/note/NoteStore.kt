package dev.sku20.stopgap.app.note

import kotlin.uuid.Uuid

interface NoteStore {

    fun create(req: NoteReq): Note

    fun list(): List<Note>

    fun get(noteId: Uuid): Note?

    fun update(noteId: Uuid, req: NoteReq): Note?

    fun delete(noteId: Uuid): Boolean

}
