package dev.sku20.stopgap.app.note.store

import dev.sku20.stopgap.app.note.NoteNotFoundException
import dev.sku20.stopgap.app.note.NoteReq
import dev.sku20.stopgap.app.note.UpdateReq
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import kotlin.uuid.Uuid

private const val ITERATIONS = 10_000

class InMemStoreStressTest {

    private val store = InMemStore()

    @Test
    fun updateRacingDeleteKeepsNoteDeleted() {
        val executor = Executors.newFixedThreadPool(2)
        // Last thread to arrive releases both, so the actions start as close together as possible.
        // "Java Concurrency in Practice" §5.5.1 starting gate; a barrier, not its latch, as it resets per iteration.
        val gate = CyclicBarrier(2)
        for (i in 0 until ITERATIONS) {
            val noteId = store.create(NoteReq("foo")).noteId

            val update = executor.submit {
                gate.await()
                updateIfPresent(noteId)
            }
            val delete = executor.submit {
                gate.await()
                store.delete(noteId)
            }
            update.get()
            delete.get()

            assertThatExceptionOfType(NoteNotFoundException::class.java)
                .describedAs("iteration $i")
                .isThrownBy { store.get(noteId) }
        }
        // Not in a finally: on failure the idle pool threads live only until the test JVM exits.
        executor.close()
    }

    // Losing the race to delete is a valid outcome.
    private fun updateIfPresent(noteId: Uuid) {
        try {
            store.update(UpdateReq(noteId, "bar"))
        } catch (e: NoteNotFoundException) {
        }
    }

}
