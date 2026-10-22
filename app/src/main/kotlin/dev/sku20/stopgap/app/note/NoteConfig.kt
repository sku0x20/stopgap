package dev.sku20.stopgap.app.note

import dev.sku20.stopgap.app.note.store.InMemStore
import dev.sku20.stopgap.app.note.store.NoteStore
import dev.sku20.stopgap.app.note.web.NoteEndpoint
import dev.sku20.stopgap.ir.annotation.Creates

object NoteConfig {

    @Creates
    fun noteEndpoint(
        noteStore: NoteStore
    ): NoteEndpoint {
        return NoteEndpoint(noteStore)
    }

    @Creates
    fun noteStore(): NoteStore {
        return InMemStore()
    }

}
