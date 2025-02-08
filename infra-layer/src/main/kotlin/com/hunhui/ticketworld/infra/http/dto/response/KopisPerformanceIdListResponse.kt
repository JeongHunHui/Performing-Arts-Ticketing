package com.hunhui.ticketworld.infra.http.dto.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class KopisPerformanceIdListResponse(
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "db")
    val ids: List<KopisPerformanceIdResponse>?,
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class KopisPerformanceIdResponse(
        @JacksonXmlProperty(localName = "mt20id")
        val id: String,
    )
}
