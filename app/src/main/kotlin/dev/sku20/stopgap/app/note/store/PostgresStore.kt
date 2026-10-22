package dev.sku20.stopgap.app.note.store

import dev.sku20.stopgap.app.note.Note
import dev.sku20.stopgap.app.note.NoteReq
import dev.sku20.stopgap.app.note.UpdateReq
import java.sql.PreparedStatement
import javax.sql.DataSource
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

private const val CREATE_TABLE = """
    CREATE TABLE IF NOT EXISTS notes (
        note_id uuid PRIMARY KEY,
        text    text NOT NULL
    )
"""

private const val INSERT_NOTE = "INSERT INTO notes (note_id, text) VALUES (?, ?)"

class PostgresStore(
    private val dataSource: DataSource,
) : NoteStore {

    init {
        createTable()
    }

    @OptIn(ExperimentalUuidApi::class)
    override fun create(req: NoteReq): Note {
        val note = Note(Uuid.generateV7(), req.text)
        withStatement(INSERT_NOTE) {
            it.setObject(1, note.noteId.toJavaUuid())
            it.setString(2, note.text)
            it.executeUpdate()
        }
        return note
    }

    override fun list(): List<Note> = TODO()

    override fun get(noteId: Uuid): Note = TODO()

    override fun update(req: UpdateReq): Note = TODO()

    override fun delete(noteId: Uuid): Unit = TODO()

    private fun createTable() {
        withStatement(CREATE_TABLE) { it.execute() }
    }

    private fun <T> withStatement(sql: String, block: (PreparedStatement) -> T): T =
        dataSource.connection.use { conn -> conn.prepareStatement(sql).use(block) }

}
