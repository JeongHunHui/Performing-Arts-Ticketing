package com.hunhui.ticketworld.infra.http.deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.hunhui.ticketworld.domain.kopis.KopisPerformanceGenre

class GenreDeserializer : JsonDeserializer<KopisPerformanceGenre>() {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext,
    ) = KopisPerformanceGenre.fromName(p.text)
}
