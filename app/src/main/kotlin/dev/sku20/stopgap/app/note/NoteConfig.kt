package dev.sku20.stopgap.app.note

import dev.sku20.stopgap.ir.annotation.Creates

object NoteConfig {

    @Creates
    fun noteEndpoint(): NoteEndpoint {
        return NoteEndpoint()
    }

}
