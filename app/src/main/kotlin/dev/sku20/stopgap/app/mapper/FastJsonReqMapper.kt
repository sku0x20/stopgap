package dev.sku20.stopgap.app.mapper

import dev.sku20.stopgap.helidon.mapper.req.ReqMapper
import dev.sku20.stopgap.helidon.mapper.req.bodyreqmapper.BodyReqMapper
import dev.sku20.stopgap.helidon.mapper.req.bodyreqmapper.json.JsonMediaTypeRule

class FastJsonReqMapper : ReqMapper by BodyReqMapper(JsonMediaTypeRule(), FastJsonDecoder())
