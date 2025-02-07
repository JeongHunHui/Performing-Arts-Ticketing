package com.hunhui.ticketworld.infra.http.dto.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.hunhui.ticketworld.domain.kopis.KopisPerformanceFacility

data class KopisPerformanceFacilityResponse(
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "db")
    val facility: PerformanceFacilityResponse,
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class PerformanceFacilityResponse(
        @JacksonXmlProperty(localName = "fcltynm")
        val name: String,
        @JacksonXmlProperty(localName = "mt10id")
        val id: String,
        @JacksonXmlProperty(localName = "telno")
        val telNo: String?,
        @JacksonXmlProperty(localName = "relateurl")
        val relateUrl: String?,
        @JacksonXmlProperty(localName = "adres")
        val address: String,
        @JacksonXmlProperty(localName = "la")
        val latitude: Double?,
        @JacksonXmlProperty(localName = "lo")
        val longitude: Double?,
        @JacksonXmlProperty(localName = "mt13")
        @JacksonXmlCData
        @JacksonXmlElementWrapper(useWrapping = false)
        val mt13s: List<PerformancePlaceResponse>?,
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        data class PerformancePlaceResponse(
            @JacksonXmlProperty(localName = "prfplcnm")
            val name: String,
            @JacksonXmlProperty(localName = "seatscale")
            val seatScale: String,
        )
    }

    fun toDomain(): KopisPerformanceFacility =
        KopisPerformanceFacility(
            name = facility.name,
            id = facility.id,
            address = facility.address,
            places =
                facility.mt13s?.map { place ->
                    KopisPerformanceFacility.Place(
                        roomName = place.name,
                        seatScale = place.seatScale,
                    )
                } ?: emptyList(),
        )
}
