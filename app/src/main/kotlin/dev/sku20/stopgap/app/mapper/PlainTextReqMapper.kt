package dev.sku20.stopgap.app.mapper

import dev.sku20.stopgap.helidon.mapper.ReqMapper
import dev.sku20.stopgap.helidon.mapper.bodyreqmapper.BodyReqMapper
import dev.sku20.stopgap.helidon.mapper.bodyreqmapper.plaintext.PlainTextDecoder
import dev.sku20.stopgap.helidon.mapper.bodyreqmapper.plaintext.PlainTextMediaTypeRule

object PlainTextReqMapper : ReqMapper by BodyReqMapper(PlainTextMediaTypeRule, PlainTextDecoder)
