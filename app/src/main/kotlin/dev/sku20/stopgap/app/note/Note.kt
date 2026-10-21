package dev.sku20.stopgap.app.note

import java.util.UUID

@JvmRecord
data class Note(
    val noteId: UUID,
    val text: String
)
