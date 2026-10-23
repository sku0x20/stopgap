package dev.sku20.stopgap.app.note.web

import io.helidon.http.HttpException
import io.helidon.http.Status

class InvalidNoteIdException(noteId: String) :
    HttpException("Invalid note id: $noteId", Status.BAD_REQUEST_400)
