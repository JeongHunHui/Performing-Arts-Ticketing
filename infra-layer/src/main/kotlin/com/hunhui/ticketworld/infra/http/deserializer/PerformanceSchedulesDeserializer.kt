package com.hunhui.ticketworld.infra.http.deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.hunhui.ticketworld.domain.kopis.PerformanceSchedule
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class PerformanceSchedulesDeserializer : JsonDeserializer<List<PerformanceSchedule>>() {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext,
    ): List<PerformanceSchedule> {
        val schedules = mutableListOf<PerformanceSchedule>()
        // 세그먼트 구분: "),"로 구분한 후 각 세그먼트에 오른쪽 괄호가 없으면 붙여줌
        val segments =
            p.text
                .split("),")
                .map { it.trim() }
                .map { if (!it.endsWith(")")) "$it)" else it }

        // 한글 요일 이름을 java.time.DayOfWeek와 매핑
        val dayOfWeekMap =
            mapOf(
                "월요일" to DayOfWeek.MONDAY,
                "화요일" to DayOfWeek.TUESDAY,
                "수요일" to DayOfWeek.WEDNESDAY,
                "목요일" to DayOfWeek.THURSDAY,
                "금요일" to DayOfWeek.FRIDAY,
                "토요일" to DayOfWeek.SATURDAY,
                "일요일" to DayOfWeek.SUNDAY,
            )

        // 정규식: 예) "금요일(11:00,14:00,17:00)"
        val regex = Regex("""(.+?)\((.+?)\)""")
        for (segment in segments) {
            val matchResult = regex.find(segment)
            if (matchResult != null) {
                val (dayPart, timesPart) = matchResult.destructured
                // 각 시간은 콤마로 구분됨
                val times =
                    timesPart
                        .split(",")
                        .map { it.trim() }
                        .map { LocalTime.parse(it, DateTimeFormatter.ofPattern("H:mm")) }

                // 요일 부분 처리
                val daySpec: PerformanceSchedule.Day =
                    when {
                        dayPart.trim().equals("HOL", ignoreCase = true) -> {
                            PerformanceSchedule.Day.Holiday
                        }
                        dayPart.contains("~") -> {
                            // 예: "토요일 ~ 일요일" -> 시작 요일과 종료 요일 사이의 모든 요일
                            val parts = dayPart.split("~").map { it.trim() }
                            val startDay = dayOfWeekMap[parts[0]] ?: error("알 수 없는 요일: ${parts[0]}")
                            val endDay = dayOfWeekMap[parts[1]] ?: error("알 수 없는 요일: ${parts[1]}")
                            val daysList = mutableListOf<DayOfWeek>()
                            var current = startDay
                            while (true) {
                                daysList.add(current)
                                if (current == endDay) break
                                current = current.plus(1) // 다음 요일
                            }
                            PerformanceSchedule.Day.WeekDays(daysList)
                        }
                        else -> {
                            // 단일 요일인 경우
                            val day = dayOfWeekMap[dayPart.trim()] ?: error("알 수 없는 요일: $dayPart")
                            PerformanceSchedule.Day.WeekDays(listOf(day))
                        }
                    }
                schedules.add(PerformanceSchedule(daySpec, times))
            }
        }
        return schedules
    }
}
