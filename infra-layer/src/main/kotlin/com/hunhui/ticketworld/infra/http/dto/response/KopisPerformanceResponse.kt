package com.hunhui.ticketworld.infra.http.dto.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.hunhui.ticketworld.domain.kopis.KopisPerformance
import com.hunhui.ticketworld.domain.kopis.KopisPerformanceGenre
import com.hunhui.ticketworld.domain.kopis.KopisPerformanceStatus
import com.hunhui.ticketworld.domain.kopis.PerformanceSchedule
import com.hunhui.ticketworld.domain.kopis.SeatGradeInfo
import com.hunhui.ticketworld.infra.http.deserializer.BooleanYNDeserializer
import com.hunhui.ticketworld.infra.http.deserializer.GenreDeserializer
import com.hunhui.ticketworld.infra.http.deserializer.LocalDateDeserializer
import com.hunhui.ticketworld.infra.http.deserializer.LocalDateTimeDeserializer
import com.hunhui.ticketworld.infra.http.deserializer.PerformanceSchedulesDeserializer
import com.hunhui.ticketworld.infra.http.deserializer.SeatGradeInfoListDeserializer
import com.hunhui.ticketworld.infra.http.deserializer.StatusDeserializer
import java.time.LocalDate
import java.time.LocalDateTime

data class KopisPerformanceResponse(
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "db")
    val db: Db,
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Db(
        @JacksonXmlProperty(localName = "mt20id")
        val id: String,
        @JacksonXmlProperty(localName = "prfnm")
        val title: String,
        @JacksonXmlProperty(localName = "prfpdfrom")
        @JsonDeserialize(using = LocalDateDeserializer::class)
        val startDate: LocalDate,
        @JacksonXmlProperty(localName = "prfpdto")
        @JsonDeserialize(using = LocalDateDeserializer::class)
        val endDate: LocalDate,
        @JacksonXmlProperty(localName = "fcltynm")
        val location: String,
        @JacksonXmlProperty(localName = "prfcast")
        val cast: String?,
        @JacksonXmlProperty(localName = "prfcrew")
        val crew: String?,
        @JacksonXmlProperty(localName = "prfruntime")
        val runtime: String?,
        @JacksonXmlProperty(localName = "prfage")
        val ageLimit: String?,
        @JacksonXmlProperty(localName = "entrpsnm")
        val enterprise: String?,
        @JacksonXmlProperty(localName = "entrpsnmP")
        val enterpriseP: String?,
        @JacksonXmlProperty(localName = "entrpsnmA")
        val enterpriseA: String?,
        @JacksonXmlProperty(localName = "entrpsnmH")
        val hostingOrganization: String?,
        @JacksonXmlProperty(localName = "entrpsnmS")
        val sponsoringOrganization: String?,
        @JacksonXmlProperty(localName = "pcseguidance")
        @JsonDeserialize(using = SeatGradeInfoListDeserializer::class)
        val seatGradeInfos: List<SeatGradeInfo>,
        @JacksonXmlProperty(localName = "poster")
        val posterUrl: String?,
        @JacksonXmlProperty(localName = "area")
        val region: String,
        @JacksonXmlProperty(localName = "genrenm")
        @JsonDeserialize(using = GenreDeserializer::class)
        val genre: KopisPerformanceGenre,
        @JacksonXmlProperty(localName = "openrun")
        @JsonDeserialize(using = BooleanYNDeserializer::class)
        val openRun: Boolean,
        @JacksonXmlProperty(localName = "updatedate")
        @JsonDeserialize(using = LocalDateTimeDeserializer::class)
        val updateDate: LocalDateTime,
        @JacksonXmlProperty(localName = "prfstate")
        @JsonDeserialize(using = StatusDeserializer::class)
        val performanceState: KopisPerformanceStatus,
        @JacksonXmlProperty(localName = "styurl")
        @JacksonXmlCData
        @JacksonXmlElementWrapper(useWrapping = false)
        val styurls: List<String>?,
        @JacksonXmlProperty(localName = "mt10id")
        val facilityId: String,
        @JacksonXmlProperty(localName = "dtguidance")
        @JsonDeserialize(using = PerformanceSchedulesDeserializer::class)
        val schedules: List<PerformanceSchedule>,
    )

    fun toDomain(): KopisPerformance =
        with(db) {
            KopisPerformance(
                id = id,
                title = title,
                startDate = startDate,
                endDate = endDate,
                location = location,
                cast = cast,
                crew = crew,
                runtime = runtime,
                ageLimit = ageLimit,
                enterprise = enterprise,
                seatGradeInfos = seatGradeInfos,
                posterUrl = posterUrl,
                region = region,
                genre = genre,
                updateDate = updateDate,
                performanceState = performanceState,
                descriptionImageUrls = styurls ?: emptyList(),
                facilityId = facilityId,
                schedules = schedules,
            )
        }
}
