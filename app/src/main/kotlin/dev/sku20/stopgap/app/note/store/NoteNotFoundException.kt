package dev.sku20.stopgap.app.note.store

import io.helidon.http.HttpException
import io.helidon.http.Status
import kotlin.uuid.Uuid

class NoteNotFoundException(noteId: Uuid) :
    HttpException("Note not found: $noteId", Status.NOT_FOUND_404)
