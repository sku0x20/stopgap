package dev.sku20.stopgap.app.note

import java.util.concurrent.ConcurrentHashMap
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class InMemStore : NoteStore {

    private val notes = ConcurrentHashMap<Uuid, Note>()

    @OptIn(ExperimentalUuidApi::class)
    override fun create(req: NoteReq): Note {
        val note = Note(Uuid.generateV7(), req.text)
        notes[note.noteId] = note
        return note
    }

    // Weakly consistent: never throws on concurrent writes, but may miss or include them; not a snapshot.
    override fun list(): List<Note> = notes.values.sortedBy { it.noteId }

    override fun get(noteId: Uuid): Note = notes[noteId] ?: throw NoteNotFoundException(noteId)

    // Atomic, unlike find-then-put, where a DELETE in between brings the note back:
    //   PATCH  (thread 1): find(id)                // exists
    //   DELETE (thread 2): notes.remove(id)
    //   PATCH  (thread 1): notes[id] = Note(...)   // deleted note is back
    override fun update(req: UpdateReq): Note =
        notes.computeIfPresent(req.noteId) { _, _ -> Note(req.noteId, req.text) }
            ?: throw NoteNotFoundException(req.noteId)

    override fun delete(noteId: Uuid) {
        notes.remove(noteId) ?: throw NoteNotFoundException(noteId)
    }

}
