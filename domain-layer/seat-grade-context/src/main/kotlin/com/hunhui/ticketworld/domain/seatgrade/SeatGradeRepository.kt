package com.hunhui.ticketworld.domain.seatgrade

import java.util.UUID

interface SeatGradeRepository {
    fun getById(id: UUID): SeatGrade

    fun findAllByIds(ids: List<UUID>): List<SeatGrade>

    fun findAllByPerformanceId(performanceId: UUID): List<SeatGrade>

    fun findAllByPerformanceIds(performanceIds: List<UUID>): Map<UUID, List<SeatGrade>>

    fun save(seatGrade: SeatGrade)

    fun saveAll(seatGrades: List<SeatGrade>)
}
