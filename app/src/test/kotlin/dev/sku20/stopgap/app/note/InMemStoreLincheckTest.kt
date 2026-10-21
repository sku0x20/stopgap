package dev.sku20.stopgap.app.note

import org.jetbrains.lincheck.datastructures.ModelCheckingOptions
import org.jetbrains.lincheck.datastructures.Operation
import org.junit.jupiter.api.Test

class InMemStoreLincheckTest {

    private val store = InMemStore()
    private val noteId = store.create(NoteReq("foo")).noteId

    // Returns text, not Note: each run gets a fresh store with a new random noteId.
    @Operation
    fun update(text: String): String = store.update(UpdateReq(noteId, text)).text

    @Operation
    fun delete() = store.delete(noteId)

    @Operation
    fun read(): String = store.get(noteId).text

    @Test
    fun modelChecking() {
        // Results alone can't expose a resurrected note: a read after both threads finish is needed.
        ModelCheckingOptions()
            // No random scenarios, only the custom one: seconds instead of minutes, but misses races it doesn't cover.
            .iterations(0)
            .addCustomScenario {
                parallel {
                    thread { actor(::update, "bar") }
                    thread { actor(::delete) }
                }
                post { actor(::read) }
            }
            .check(this::class)
    }

}
