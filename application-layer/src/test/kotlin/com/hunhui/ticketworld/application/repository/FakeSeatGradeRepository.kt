package com.hunhui.ticketworld.application.repository

import com.hunhui.ticketworld.domain.seatgrade.SeatGrade
import com.hunhui.ticketworld.domain.seatgrade.SeatGradeRepository
import java.util.UUID

class FakeSeatGradeRepository : SeatGradeRepository {
    private val seatGrades = mutableListOf<SeatGrade>()

    override fun save(seatGrade: SeatGrade) {
        seatGrades.add(seatGrade)
    }

    override fun saveAll(seatGrades: List<SeatGrade>) {
        this.seatGrades.addAll(seatGrades)
    }

    override fun getById(id: UUID): SeatGrade {
        TODO("Not yet implemented")
    }

    override fun findAllByIds(ids: List<UUID>): List<SeatGrade> {
        TODO("Not yet implemented")
    }

    override fun findAllByPerformanceId(performanceId: UUID): List<SeatGrade> = seatGrades.filter { it.performanceId == performanceId }
}
