package com.hunhui.ticketworld.domain.kopis

enum class KopisPerformanceGenre(
    val genreName: String,
    val genreCode: String,
) {
    THEATER("연극", "AAAA"),
    DANCING("무용", "BBBC"),
    PUBLIC_DANCING("대중무용", "BBBC"),
    CONCERT("대중음악", "CCCD"),
    MUSICAL("뮤지컬", "GGGA"),
    CLASSIC("서양음악(클래식)", "CCCA"),
    TRADITIONAL("한국음악(국악)", "CCCC"),
    COMPOSITE("복합", "EEEA"),
    CIRCUS("서커스/마술", "EEEB"), ;

    companion object {
        fun fromName(genreName: String): KopisPerformanceGenre = entries.first { it.genreName == genreName }
    }
}
