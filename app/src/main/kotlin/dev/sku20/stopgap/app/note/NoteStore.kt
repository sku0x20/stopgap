package dev.sku20.stopgap.app.note

import kotlin.uuid.Uuid

interface NoteStore {

    fun create(text: String): Note

    fun list(): List<Note>

    fun get(noteId: Uuid): Note?

    fun updateText(noteId: Uuid, text: String): Note?

    fun delete(noteId: Uuid): Boolean

}
