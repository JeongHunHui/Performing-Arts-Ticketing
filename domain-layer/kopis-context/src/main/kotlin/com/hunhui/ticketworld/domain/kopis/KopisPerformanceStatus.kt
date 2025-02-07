package com.hunhui.ticketworld.domain.kopis

enum class KopisPerformanceStatus(
    val statusName: String,
    val statusCode: String,
) {
    SCHEDULED("공연예정", "01"),
    IN_PROGRESS("공연중", "02"),
    COMPLETED("공연완료", "03"),
    ;

    companion object {
        fun fromName(genreName: String): KopisPerformanceStatus = entries.first { it.statusName == genreName }
    }
}
