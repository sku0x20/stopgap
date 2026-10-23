package dev.sku20.stopgap.app.note.store

import dev.sku20.stopgap.app.note.Note
import dev.sku20.stopgap.app.note.NoteReq
import dev.sku20.stopgap.app.note.UpdateReq
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

class InMemStoreTest {

    private val store = InMemStore()

    @Test
    fun create() {
        val note = store.create(NoteReq("foo"))
        assertThat(note.text).isEqualTo("foo")
    }

    @Test
    fun get() {
        val created = store.create(NoteReq("foo"))
        assertThat(store.get(created.noteId)).isEqualTo(created)
    }

    @Test
    fun list() {
        val first = store.create(NoteReq("foo"))
        val second = store.create(NoteReq("bar"))
        assertThat(store.list())
            .containsExactlyInAnyOrder(first, second)
            .isSortedAccordingTo(compareBy { it.noteId })
    }

    @Test
    fun update() {
        val created = store.create(NoteReq("foo"))

        val updated = store.update(UpdateReq(created.noteId, "bar"))

        assertThat(updated).isEqualTo(Note(created.noteId, "bar"))
        assertThat(store.get(created.noteId)).isEqualTo(updated)
    }

    @Test
    fun delete() {
        val created = store.create(NoteReq("foo"))

        store.delete(created.noteId)

        assertThatThrownBy { store.get(created.noteId) }.isInstanceOf(NoteNotFoundException::class.java)
    }

    @Test
    fun getMissing() {
        assertThatThrownBy { store.get(Uuid.random()) }.isInstanceOf(NoteNotFoundException::class.java)
    }

    @Test
    fun updateMissing() {
        assertThatThrownBy { store.update(UpdateReq(Uuid.random(), "foo")) }
            .isInstanceOf(NoteNotFoundException::class.java)
    }

    @Test
    fun deleteMissing() {
        assertThatThrownBy { store.delete(Uuid.random()) }.isInstanceOf(NoteNotFoundException::class.java)
    }

}
