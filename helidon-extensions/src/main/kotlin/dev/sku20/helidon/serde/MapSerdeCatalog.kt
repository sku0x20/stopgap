package dev.sku20.helidon.serde

import io.helidon.http.HttpException
import io.helidon.http.HttpMediaType
import io.helidon.http.Status

class MapSerdeCatalog : SerdeCatalog {

    private val serdes = HashMap<HttpMediaType, Serde>()

    fun add(serde: Serde) {
        require(serde.mediaType !in serdes) { "Serde already registered for ${serde.mediaType}" }
        serdes[serde.mediaType] = serde
    }

    override fun get(mediaType: HttpMediaType): Serde {
        return serdes[mediaType]
            ?: throw HttpException("No serde registered for $mediaType", Status.UNSUPPORTED_MEDIA_TYPE_415)
    }
}
