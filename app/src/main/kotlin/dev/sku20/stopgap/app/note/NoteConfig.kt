package dev.sku20.stopgap.app.note

import com.zaxxer.hikari.HikariDataSource
import dev.sku20.stopgap.app.note.store.NoteStore
import dev.sku20.stopgap.app.note.store.PostgresStore
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
    fun noteStore(dataSource: HikariDataSource): NoteStore {
        return PostgresStore(dataSource)
    }

}
