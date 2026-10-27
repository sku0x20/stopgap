package dev.sku20.stopgap.helidon.mapper.req.bodyreqmapper.json

import dev.sku20.stopgap.helidon.mapper.req.ReqMapper
import dev.sku20.stopgap.helidon.mapper.req.bodyreqmapper.BodyDecoder
import dev.sku20.stopgap.helidon.mapper.req.bodyreqmapper.BodyReqMapper

class JsonReqMapper(decoder: BodyDecoder) : ReqMapper by BodyReqMapper(JsonMediaTypeRule(), decoder)
