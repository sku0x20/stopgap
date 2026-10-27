package dev.sku20.stopgap.helidon.mapper.res.bodyresmapper.json

import dev.sku20.stopgap.helidon.mapper.res.ResMapper
import dev.sku20.stopgap.helidon.mapper.res.bodyresmapper.BodyEncoder
import dev.sku20.stopgap.helidon.mapper.res.bodyresmapper.BodyResMapper
import io.helidon.http.HttpMediaTypes

class JsonResMapper(encoder: BodyEncoder) : ResMapper by BodyResMapper(HttpMediaTypes.JSON, encoder)
