package com.hunhui.ticketworld.domain.kopis

import java.time.LocalDate
import java.time.LocalDateTime

data class KopisPerformance(
    val id: String,
    val name: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val location: String,
    /** 출연진 */
    val cast: String?,
    /** 제작진 */
    val crew: String?,
    val runtime: String?,
    val ageLimit: String?,
    /** 기획제작사 */
    val enterprise: String?,
    val ticketPriceInfo: String,
    val posterUrl: String?,
    /** 지역 */
    val region: String,
    val genre: KopisPerformanceGenre,
    val updateDate: LocalDateTime,
    val performanceState: KopisPerformanceStatus,
    val styleUrls: List<String>,
    val facilityId: String,
    val dateGuidance: String,
)
