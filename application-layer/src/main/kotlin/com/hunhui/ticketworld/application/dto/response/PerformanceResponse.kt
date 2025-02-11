package com.hunhui.ticketworld.application.dto.response

import com.hunhui.ticketworld.domain.performance.Performance
import com.hunhui.ticketworld.domain.performance.PerformanceGenre
import com.hunhui.ticketworld.domain.seatgrade.SeatGrade
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class PerformanceResponse(
    val id: UUID,
    val title: String,
    val startDate: LocalDate,
    val finishDate: LocalDate,
    val genre: PerformanceGenre,
    val posterUrl: String?,
    val location: String,
    val descriptionImageUrls: List<String>,
    val runtime: String?,
    val ageLimit: String?,
    val minimumReservationStartTime: LocalDateTime,
    val maxReservationCount: Int,
    val seatGrades: List<SeatGradeResponse>,
    val rounds: List<PerformanceRoundResponse>,
) {
    companion object {
        fun from(
            performance: Performance,
            seatGrades: List<SeatGrade>,
        ): PerformanceResponse =
            PerformanceResponse(
                id = performance.id,
                title = performance.info.title,
                startDate = performance.startDate,
                finishDate = performance.finishDate,
                genre = performance.info.genre,
                posterUrl = performance.info.posterUrl,
                location = performance.info.location,
                descriptionImageUrls = performance.info.descriptionImageUrls,
                runtime = performance.info.runtime,
                ageLimit = performance.info.ageLimit,
                minimumReservationStartTime = performance.minimumReservationStartTime,
                maxReservationCount = performance.maxReservationCount,
                seatGrades =
                    seatGrades.map {
                        SeatGradeResponse(
                            id = it.id,
                            name = it.name,
                            price = it.price.amount,
                        )
                    },
                rounds =
                    performance.availableRounds.map {
                        PerformanceRoundResponse(
                            id = it.id,
                            roundStartTime = it.roundStartTime,
                        )
                    },
            )
    }

    data class SeatGradeResponse(
        val id: UUID,
        val name: String,
        val price: Long,
    )

    data class PerformanceRoundResponse(
        val id: UUID,
        val roundStartTime: LocalDateTime,
    )
}
