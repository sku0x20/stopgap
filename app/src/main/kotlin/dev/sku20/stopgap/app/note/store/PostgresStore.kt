package dev.sku20.stopgap.app.note.store

import dev.sku20.stopgap.app.note.Note
import dev.sku20.stopgap.app.note.NoteReq
import dev.sku20.stopgap.app.note.UpdateReq
import java.sql.ResultSet
import java.util.*
import javax.sql.DataSource
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

class PostgresStore(
    private val dataSource: DataSource,
) : NoteStore {

    init {
        createTable()
    }

    @OptIn(ExperimentalUuidApi::class)
    override fun create(req: NoteReq): Note {
        val note = Note(Uuid.generateV7(), req.text)
        dataSource.withConnection {
            withStatement(INSERT_NOTE) {
                it.setObject(1, note.noteId.toJavaUuid())
                it.setString(2, note.text)
                it.executeUpdate()
            }
        }
        return note
    }

    override fun list(): List<Note> = dataSource.withConnection {
        withStatement(SELECT_NOTES) { it.executeQuery().map(::mapNote) }
    }

    override fun get(noteId: Uuid): Note = dataSource.withConnection {
        withStatement(SELECT_NOTE) {
            it.setObject(1, noteId.toJavaUuid())
            it.executeQuery().firstOrThrow(::mapNote) { NoteNotFoundException(noteId) }
        }
    }

    override fun update(req: UpdateReq): Note = dataSource.withConnection {
        withStatement(UPDATE_NOTE) {
            it.setString(1, req.text)
            it.setObject(2, req.noteId.toJavaUuid())
            it.executeQuery().firstOrThrow(::mapNote) { NoteNotFoundException(req.noteId) }
        }
    }

    override fun delete(noteId: Uuid): Unit = dataSource.withConnection {
        withStatement(DELETE_NOTE) {
            it.setObject(1, noteId.toJavaUuid())
            if (it.executeUpdate() == 0) throw NoteNotFoundException(noteId)
        }
    }

    private fun createTable(): Unit = dataSource.withConnection {
        withStatement(CREATE_TABLE) { it.execute() }
    }

    private fun mapNote(rs: ResultSet): Note = Note(
        rs.getObject("note_id", UUID::class.java).toKotlinUuid(),
        rs.getString("text"),
    )

    // Statements
    private companion object {

        const val CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS notes (
                note_id uuid PRIMARY KEY,
                text    text NOT NULL
            )
        """

        const val INSERT_NOTE = "INSERT INTO notes (note_id, text) VALUES (?, ?)"

        const val SELECT_NOTES = "SELECT note_id, text FROM notes ORDER BY note_id"

        const val SELECT_NOTE = "SELECT note_id, text FROM notes WHERE note_id = ?"

        const val UPDATE_NOTE = "UPDATE notes SET text = ? WHERE note_id = ? RETURNING note_id, text"

        const val DELETE_NOTE = "DELETE FROM notes WHERE note_id = ?"

    }

}
