package com.hunhui.ticketworld.application.repository

import com.hunhui.ticketworld.common.error.BusinessException
import com.hunhui.ticketworld.domain.performance.Performance
import com.hunhui.ticketworld.domain.performance.PerformanceRepository
import com.hunhui.ticketworld.domain.performance.exception.PerformanceErrorCode.NOT_FOUND
import java.util.UUID

class FakePerformanceRepository : PerformanceRepository {
    // 테스트용 in-memory 티켓 목록
    private val performances = mutableMapOf<UUID, Performance>()

    override fun save(performance: Performance) {
        performances[performance.id] = performance
    }

    override fun getById(id: UUID): Performance = performances[id] ?: throw BusinessException(NOT_FOUND)

    override fun getByIdAndRoundId(
        performanceId: UUID,
        roundId: UUID,
    ): Performance {
        performances[performanceId]?.let { performance ->
            if (performance.rounds.any { it.id == roundId }) return performance
        }
        throw BusinessException(NOT_FOUND)
    }

    override fun findByKopisId(kopisId: String): Performance? {
        TODO("Not yet implemented")
    }

    override fun findAllWithPagenation(
        page: Int,
        size: Int,
    ): Pair<List<Performance>, Int> {
        TODO("Not yet implemented")
    }
}
