package dev.sku20.stopgap.app.note

import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import kotlin.uuid.Uuid

private const val ITERATIONS = 10_000

class InMemStoreStressTest {

    private val store = InMemStore()

    @Test
    fun updateRacingDeleteKeepsNoteDeleted() {
        val executor = Executors.newFixedThreadPool(2)
        try {
            for (i in 0 until ITERATIONS) {
                val noteId = store.create(NoteReq("foo")).noteId
                val gate = CountDownLatch(2)

                val update = executor.submit { startTogether(gate) { updateIfPresent(noteId) } }
                val delete = executor.submit { startTogether(gate) { store.delete(noteId) } }
                update.get()
                delete.get()

                assertThatExceptionOfType(NoteNotFoundException::class.java)
                    .describedAs("iteration $i")
                    .isThrownBy { store.get(noteId) }
            }
        } finally {
            executor.close()
        }
    }

    // Last thread to arrive releases all, so the actions start as close together as possible.
    private fun startTogether(gate: CountDownLatch, action: () -> Unit) {
        gate.countDown()
        gate.await()
        action()
    }

    // Losing the race to delete is a valid outcome.
    private fun updateIfPresent(noteId: Uuid) {
        try {
            store.update(UpdateReq(noteId, "bar"))
        } catch (e: NoteNotFoundException) {
        }
    }

}
