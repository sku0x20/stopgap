package dev.sku20.stopgap.app.note

import dev.sku20.stopgap.app.note.store.NoteStore
import dev.sku20.stopgap.app.note.store.PostgresStore
import dev.sku20.stopgap.app.note.web.NoteEndpoint
import dev.sku20.stopgap.ir.annotation.Creates
import io.opentelemetry.api.OpenTelemetry
import javax.sql.DataSource

object NoteConfig {

    @Creates
    fun noteEndpoint(
        noteStore: NoteStore,
        noteMetrics: NoteMetrics
    ): NoteEndpoint {
        return NoteEndpoint(noteStore, noteMetrics)
    }

    @Creates
    fun noteMetrics(openTelemetry: OpenTelemetry): NoteMetrics {
        return NoteMetrics(openTelemetry)
    }

    @Creates
    fun noteStore(dataSource: DataSource): NoteStore {
        return PostgresStore(dataSource)
    }

}
