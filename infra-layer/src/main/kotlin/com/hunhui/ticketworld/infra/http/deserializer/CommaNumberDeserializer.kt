package com.hunhui.ticketworld.infra.http.deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer

class CommaNumberDeserializer : JsonDeserializer<Int>() {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext,
    ) = p.text?.replace(",", "")?.toInt()
}
