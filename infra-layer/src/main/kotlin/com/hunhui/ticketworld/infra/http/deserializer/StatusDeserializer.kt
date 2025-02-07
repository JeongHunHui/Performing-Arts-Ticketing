package com.hunhui.ticketworld.infra.http.deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.hunhui.ticketworld.domain.kopis.KopisPerformanceStatus

class StatusDeserializer : JsonDeserializer<KopisPerformanceStatus>() {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext,
    ) = KopisPerformanceStatus.fromName(p.text)
}
