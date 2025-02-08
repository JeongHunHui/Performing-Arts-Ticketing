package com.hunhui.ticketworld.domain.kopis

import java.time.DayOfWeek
import java.time.LocalTime

data class PerformanceSchedule(
    val day: Day,
    val times: List<LocalTime>,
) {
    sealed class Day {
        data class WeekDays(
            val days: List<DayOfWeek>,
        ) : Day()

        data object Holiday : Day()
    }
}
