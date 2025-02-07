package com.hunhui.ticketworld.infra.http.deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer

class BooleanYNDeserializer : JsonDeserializer<Boolean>() {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext,
    ) = when (p.text?.uppercase()) {
        "Y" -> true
        "N" -> false
        else -> throw IllegalArgumentException("Unexpected value: ${p.text}")
    }
}
