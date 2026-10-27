package dev.sku20.stopgap.helidon.mapper.req.bodyreqmapper.plaintext

import dev.sku20.stopgap.helidon.mapper.req.ReqMapper
import dev.sku20.stopgap.helidon.mapper.req.bodyreqmapper.BodyReqMapper

object PlainTextReqMapper : ReqMapper by BodyReqMapper(PlainTextMediaTypeRule, PlainTextDecoder)
