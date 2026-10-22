package dev.sku20.stopgap.app.note.store

import dev.sku20.stopgap.app.note.Note
import dev.sku20.stopgap.app.note.NoteReq
import dev.sku20.stopgap.app.note.UpdateReq
import javax.sql.DataSource
import kotlin.uuid.Uuid

private const val CREATE_TABLE = """
    CREATE TABLE IF NOT EXISTS notes (
        note_id uuid PRIMARY KEY,
        text    text NOT NULL
    )
"""

class PostgresStore(
    private val dataSource: DataSource,
) : NoteStore {

    init {
        createTable()
    }

    override fun create(req: NoteReq): Note = TODO()

    override fun list(): List<Note> = TODO()

    override fun get(noteId: Uuid): Note = TODO()

    override fun update(req: UpdateReq): Note = TODO()

    override fun delete(noteId: Uuid): Unit = TODO()

    private fun createTable() {
        dataSource.connection.use { conn ->
            conn.createStatement().use { it.execute(CREATE_TABLE) }
        }
    }

}
