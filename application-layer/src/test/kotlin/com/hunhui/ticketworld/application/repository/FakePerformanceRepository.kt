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

    override fun findAllWithPagenation(
        page: Int,
        size: Int,
    ): List<Performance> {
        TODO("Not yet implemented")
    }
}
