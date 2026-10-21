package dev.sku20.stopgap.app.note

import kotlin.uuid.Uuid

@JvmRecord
data class Note(
    val noteId: Uuid,
    val text: String
)
