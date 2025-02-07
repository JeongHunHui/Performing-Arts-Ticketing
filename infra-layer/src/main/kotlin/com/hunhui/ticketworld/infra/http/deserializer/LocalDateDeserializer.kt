package com.hunhui.ticketworld.infra.http.deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.time.LocalDate

class LocalDateDeserializer : JsonDeserializer<LocalDate>() {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext,
    ): LocalDate? =
        if (p.text == null) {
            null
        } else {
            p.text.split(".").let {
                LocalDate.of(it[0].toInt(), it[1].toInt(), it[2].toInt())
            }
        }
}
