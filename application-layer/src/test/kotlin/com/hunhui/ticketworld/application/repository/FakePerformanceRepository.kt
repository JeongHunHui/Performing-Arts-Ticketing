package com.hunhui.ticketworld.application.repository

import com.hunhui.ticketworld.domain.performance.Performance
import com.hunhui.ticketworld.domain.performance.PerformanceRepository
import java.util.UUID

class FakePerformanceRepository : PerformanceRepository {
    // 테스트용 in-memory 티켓 목록
    private val performances = mutableListOf<Performance>()

    fun addPerformance(performance: Performance) {
        performances.add(performance)
    }

    override fun getById(id: UUID): Performance = performances.first { it.id == id }

    override fun findAll(
        page: Int,
        size: Int,
    ): List<Performance> {
        TODO("Not yet implemented")
    }

    override fun save(performance: Performance) {
        TODO("Not yet implemented")
    }
}
