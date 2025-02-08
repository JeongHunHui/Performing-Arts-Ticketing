package com.hunhui.ticketworld.domain.performance

import java.util.UUID

interface PerformanceRepository {
    fun getById(id: UUID): Performance

    fun findByKopisId(kopisId: String): Performance?

    fun findAll(
        page: Int,
        size: Int,
    ): List<Performance>

    fun save(performance: Performance)
}
