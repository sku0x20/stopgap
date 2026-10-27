package dev.sku20.stopgap.helidon.mapper.res.bodyresmapper.plaintext

import dev.sku20.stopgap.helidon.mapper.res.ResMapper
import dev.sku20.stopgap.helidon.mapper.res.bodyresmapper.BodyResMapper
import io.helidon.http.HttpMediaTypes

class PlainTextResMapper : ResMapper by BodyResMapper(HttpMediaTypes.PLAINTEXT_UTF_8, PlainTextEncoder())
