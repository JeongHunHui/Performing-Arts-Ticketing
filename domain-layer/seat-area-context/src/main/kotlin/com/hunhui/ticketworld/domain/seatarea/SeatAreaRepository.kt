package com.hunhui.ticketworld.domain.seatarea

import java.util.UUID

interface SeatAreaRepository {
    fun findByPerformanceId(performanceId: UUID): List<SeatArea>

    fun findAllByPerformanceIds(performanceIds: List<UUID>): Map<UUID, List<SeatArea>>

    fun saveAll(seatAreas: List<SeatArea>)
}
