package dev.sku20.stopgap.app.mapper

import dev.sku20.stopgap.helidon.mapper.req.ReqMapper
import dev.sku20.stopgap.helidon.mapper.req.bodyreqmapper.BodyReqMapper
import dev.sku20.stopgap.helidon.mapper.req.bodyreqmapper.plaintext.PlainTextDecoder
import dev.sku20.stopgap.helidon.mapper.req.bodyreqmapper.plaintext.PlainTextMediaTypeRule

object PlainTextReqMapper : ReqMapper by BodyReqMapper(PlainTextMediaTypeRule, PlainTextDecoder)
