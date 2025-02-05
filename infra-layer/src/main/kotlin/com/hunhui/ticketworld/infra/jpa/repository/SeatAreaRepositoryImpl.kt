package com.hunhui.ticketworld.infra.jpa.repository

import com.hunhui.ticketworld.domain.seatarea.SeatArea
import com.hunhui.ticketworld.domain.seatarea.SeatAreaRepository
import com.hunhui.ticketworld.domain.seatarea.SeatPosition
import com.hunhui.ticketworld.infra.jpa.entity.SeatAreaEntity
import com.hunhui.ticketworld.infra.jpa.entity.SeatPositionEntity
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
internal class SeatAreaRepositoryImpl(
    private val seatAreaJpaRepository: SeatAreaJpaRepository,
) : SeatAreaRepository {
    override fun findByPerformanceId(performanceId: UUID): List<SeatArea> =
        seatAreaJpaRepository.findByPerformanceId(performanceId).map {
            it.domain
        }

    override fun saveAll(seatAreas: List<SeatArea>) {
        seatAreaJpaRepository.saveAll(seatAreas.map { it.entity })
    }

    private val SeatAreaEntity.domain: SeatArea
        get() =
            SeatArea(
                id = id,
                performanceId = performanceId,
                floorName = floorName,
                areaName = areaName,
                width = width,
                height = height,
                positions =
                    positions.map {
                        SeatPosition(
                            id = it.id,
                            seatGradeId = it.seatGradeId,
                            name = it.name,
                            x = it.x,
                            y = it.y,
                        )
                    },
            )

    private val SeatArea.entity: SeatAreaEntity
        get() =
            SeatAreaEntity(
                id = id,
                performanceId = performanceId,
                floorName = floorName,
                areaName = areaName,
                width = width,
                height = height,
                positions =
                    positions.map {
                        SeatPositionEntity(
                            id = it.id,
                            seatAreaId = id,
                            seatGradeId = it.seatGradeId,
                            name = it.name,
                            x = it.x,
                            y = it.y,
                        )
                    },
            )
}
