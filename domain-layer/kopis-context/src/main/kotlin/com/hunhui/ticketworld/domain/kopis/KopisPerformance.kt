package com.hunhui.ticketworld.domain.kopis

import java.time.LocalDate
import java.time.LocalDateTime

data class KopisPerformance(
    val id: String,
    val name: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val location: String,
    val cast: String?,
    val crew: String?,
    val runtime: String?,
    val ageLimit: String?,
    val enterprise: String?,
    val enterpriseP: String?,
    val enterpriseA: String?,
    val hostingOrganization: String?,
    val sponsoringOrganization: String?,
    val ticketPriceInfo: String,
    val posterUrl: String?,
    val region: String,
    val genre: KopisPerformanceGenre,
    val openRun: Boolean,
    val updateDate: LocalDateTime,
    val performanceState: KopisPerformanceStatus,
    val styleUrls: List<String>?,
    val facilityId: String,
    val dateGuidance: String,
    val relatedLinks: List<RelatedLink>?,
) {
    data class RelatedLink(
        val name: String,
        val url: String?,
    )
}
