package com.hunhui.ticketworld.infra.http.deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.time.LocalDateTime

class LocalDateTimeDeserializer : JsonDeserializer<LocalDateTime>() {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext,
    ): LocalDateTime? =
        if (p.text == null) {
            null
        } else {
            p.text.split(" ").let {
                val dates = it[0].split("-")
                val times = it[1].split(":")
                LocalDateTime.of(
                    dates[0].toInt(),
                    dates[1].toInt(),
                    dates[2].toInt(),
                    times[0].toInt(),
                    times[1].toInt(),
                    times[2].toInt(),
                )
            }
        }
}
