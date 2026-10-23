package dev.sku20.stopgap.app.note

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.metrics.LongCounter

// Instruments are built once here; building one per call would repeat the SDK's registration lookup.
class NoteMetrics(openTelemetry: OpenTelemetry) {

    private val created: LongCounter = openTelemetry.getMeter("dev.sku20.stopgap.app.note")
        .counterBuilder("notes.created")
        .setDescription("Notes created")
        .setUnit("{note}")
        .build()

    fun recordCreated() {
        created.add(1)
    }
}
