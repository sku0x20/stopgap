package dev.sku20.stopgap.app.note.store

import dev.sku20.stopgap.app.note.Note
import dev.sku20.stopgap.app.note.NoteReq
import dev.sku20.stopgap.app.note.UpdateReq
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

    override fun list(): List<Note> {
        return dataSource.withConnection {
            prepareStatement(SELECT_NOTES).use {
                val rs = it.executeQuery()
                val notes = mutableListOf<Note>()
                while (rs.next()) {
                    notes.add(
                        Note(
                            rs.getObject("note_id", UUID::class.java).toKotlinUuid(),
                            rs.getString("text"),
                        )
                    )
                }
                notes
            }
        }
    }

    override fun get(noteId: Uuid): Note {
        return dataSource.withConnection {
            prepareStatement(SELECT_NOTE).use {
                it.setObject(1, noteId.toJavaUuid())
                val rs = it.executeQuery()
                if (!rs.next()) throw NoteNotFoundException(noteId)
                Note(
                    rs.getObject("note_id", UUID::class.java).toKotlinUuid(),
                    rs.getString("text"),
                )
            }
        }
    }

    override fun update(req: UpdateReq): Note {
        return dataSource.withConnection {
            prepareStatement(UPDATE_NOTE).use {
                it.setString(1, req.text)
                it.setObject(2, req.noteId.toJavaUuid())
                val rs = it.executeQuery()
                if (!rs.next()) throw NoteNotFoundException(req.noteId)
                Note(
                    rs.getObject("note_id", UUID::class.java).toKotlinUuid(),
                    rs.getString("text"),
                )
            }
        }
    }

    override fun delete(noteId: Uuid) {
        dataSource.withConnection {
            prepareStatement(DELETE_NOTE).use {
                it.setObject(1, noteId.toJavaUuid())
                if (it.executeUpdate() == 0) throw NoteNotFoundException(noteId)
            }
        }
    }

    private fun createTable() {
        dataSource.withConnection {
            createStatement().use { it.execute(CREATE_TABLE) }
        }
    }

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
