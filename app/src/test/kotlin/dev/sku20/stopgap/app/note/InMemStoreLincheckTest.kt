package dev.sku20.stopgap.app.note

import org.jetbrains.lincheck.datastructures.ModelCheckingOptions
import org.jetbrains.lincheck.datastructures.Operation
import org.junit.jupiter.api.Test

/**
 * Checks that [InMemStore] is linearizable, using Lincheck's model checker.
 *
 * Linearizable = behaves as if one at a time: each operation appears to take effect at a single instant (its
 * linearization point) between its call and its return.
 * - Overlapping calls may take effect in either order:
 *   ```
 *   thread 1:  |---- update("bar") ----|
 *   thread 2:        |---- delete() ----|      either order is legal
 *   ```
 * - A call that returned before another started must be visible to it:
 *   ```
 *   thread 1:  |-- delete() --|
 *   thread 2:                    |-- read() --|  must not see the note
 *   ```
 * `computeIfPresent` has one such instant. Find-then-put has two (find, put), and a delete in the gap resurrects
 * the note.
 *
 * How Lincheck checks it:
 * 1. Rewrites bytecode to add a switch point before every shared access, then runs the threads one at a time,
 *    switching at different points on each invocation.
 * 2. Records each operation's return value, or its thrown exception, as its result.
 * 3. Passes if some sequential order of the operations, replayed on a fresh instance, gives the same results:
 *    ```
 *    order                     update                 delete  read
 *    update → delete → read    bar                    void    NoteNotFoundException
 *    delete → update → read    NoteNotFoundException  void    NoteNotFoundException
 *    find-then-put run         bar                    void    bar                     ✗ matches neither
 *    ```
 *
 * References:
 * - Herlihy & Wing, "Linearizability: A Correctness Condition for Concurrent Objects" (1990).
 * - Herlihy & Shavit, "The Art of Multiprocessor Programming": the classic book on linearizability.
 * - Clarke, Emerson & Sifakis: model checking, 2007 Turing Award.
 * - Koval et al., "Lincheck: A Practical Framework for Testing Concurrent Data Structures on JVM" (CAV 2023).
 * - Musuvathi & Qadeer, CHESS and iterative context bounding (2007): most races need only a few thread switches.
 * - Kleppmann, "Designing Data-Intensive Applications", ch. 9: the same guarantee across database replicas.
 */
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
