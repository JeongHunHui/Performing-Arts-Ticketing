package com.hunhui.ticketworld.application.repository

import com.hunhui.ticketworld.domain.seatgrade.SeatGrade
import com.hunhui.ticketworld.domain.seatgrade.SeatGradeRepository
import java.util.UUID

class FakeSeatGradeRepository : SeatGradeRepository {
    private val seatGrades = mutableMapOf<UUID, SeatGrade>()

    override fun save(seatGrade: SeatGrade) {
        seatGrades[seatGrade.id] = seatGrade
    }

    override fun saveAll(seatGrades: List<SeatGrade>) {
        seatGrades.forEach { save(it) }
    }

    override fun getById(id: UUID): SeatGrade {
        TODO("Not yet implemented")
    }

    override fun findAllByIds(ids: List<UUID>): List<SeatGrade> {
        TODO("Not yet implemented")
    }

    override fun findAllByPerformanceId(performanceId: UUID): List<SeatGrade> =
        seatGrades
            .filter { (_, seatGrade) ->
                seatGrade.performanceId ==
                    performanceId
            }.values
            .toList()
}
