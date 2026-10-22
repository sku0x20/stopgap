package dev.sku20.stopgap.app.note.store

import dev.sku20.stopgap.app.note.Note
import dev.sku20.stopgap.app.note.NoteReq
import dev.sku20.stopgap.app.note.UpdateReq
import javax.sql.DataSource
import kotlin.uuid.Uuid

class PostgresStore(
    private val dataSource: DataSource,
) : NoteStore {

    override fun create(req: NoteReq): Note = TODO()

    override fun list(): List<Note> = TODO()

    override fun get(noteId: Uuid): Note = TODO()

    override fun update(req: UpdateReq): Note = TODO()

    override fun delete(noteId: Uuid): Unit = TODO()

}
