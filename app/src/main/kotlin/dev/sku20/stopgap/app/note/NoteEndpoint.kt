package dev.sku20.stopgap.app.note

import dev.sku20.stopgap.helidon.endpoint.Delete
import dev.sku20.stopgap.helidon.endpoint.Endpoint
import dev.sku20.stopgap.helidon.endpoint.Get
import dev.sku20.stopgap.helidon.endpoint.Post
import dev.sku20.stopgap.helidon.endpoint.Put
import dev.sku20.stopgap.helidon.param.PathParam

@Endpoint("/notes")
class NoteEndpoint {

    @Post("/")
    fun create(body: NoteReq): Note {
        TODO()
    }

    @Get("/")
    fun list(): List<Note> {
        TODO()
    }

    @Get("/{noteId}")
    fun get(@PathParam("noteId") noteId: String): Note {
        TODO()
    }

    @Put("/{noteId}")
    fun update(@PathParam("noteId") noteId: String, body: NoteReq): Note {
        TODO()
    }

    @Delete("/{noteId}")
    fun delete(@PathParam("noteId") noteId: String) {
        TODO()
    }

}
