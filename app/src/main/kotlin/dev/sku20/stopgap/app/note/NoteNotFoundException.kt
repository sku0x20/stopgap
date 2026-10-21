package dev.sku20.stopgap.app.note

import kotlin.uuid.Uuid

class NoteNotFoundException(val noteId: Uuid) : RuntimeException("Note not found: $noteId")
