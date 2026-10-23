package dev.sku20.stopgap.app.note

import kotlin.uuid.Uuid

@JvmRecord
data class UpdateReq(
    val noteId: Uuid,
    val text: String
)
