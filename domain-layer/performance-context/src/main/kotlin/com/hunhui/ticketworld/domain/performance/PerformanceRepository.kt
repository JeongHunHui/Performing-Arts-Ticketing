package com.hunhui.ticketworld.domain.performance

import java.util.UUID

interface PerformanceRepository {
    fun getById(id: UUID): Performance

    fun findByKopisId(kopisId: String): Performance?

    fun findAllWithPagenation(
        page: Int,
        size: Int,
    ): Pair<List<Performance>, Int>

    fun save(performance: Performance)
}
