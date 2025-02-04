package com.hunhui.ticketworld.infra.jpa.repository

import com.hunhui.ticketworld.common.error.BusinessException
import com.hunhui.ticketworld.common.vo.Money
import com.hunhui.ticketworld.domain.performance.Performance
import com.hunhui.ticketworld.domain.performance.PerformancePrice
import com.hunhui.ticketworld.domain.performance.PerformanceRepository
import com.hunhui.ticketworld.domain.performance.PerformanceRound
import com.hunhui.ticketworld.domain.performance.exception.PerformanceErrorCode
import com.hunhui.ticketworld.infra.jpa.entity.PerformanceEntity
import com.hunhui.ticketworld.infra.jpa.entity.PerformancePriceEntity
import com.hunhui.ticketworld.infra.jpa.entity.PerformanceRoundEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
internal class PerformanceRepositoryImpl(
    private val performanceJpaRepository: PerformanceJpaRepository,
) : PerformanceRepository {
    override fun getById(id: UUID): Performance =
        performanceJpaRepository.findByIdOrNull(id)?.domain ?: throw BusinessException(PerformanceErrorCode.NOT_FOUND)

    override fun findAll(
        page: Int,
        size: Int,
    ): List<Performance> =
        performanceJpaRepository
            .findAll(
                Pageable.ofSize(size).withPage(page),
            ).content
            .map {
                it.domain
            }

    override fun save(performance: Performance) {
        performanceJpaRepository.save(performance.entity)
    }

    private val PerformanceEntity.domain: Performance
        get() =
            Performance(
                id = id,
                title = title,
                description = description,
                genre = genre,
                imageUrl = imageUrl,
                location = location,
                maxReservationCount = maxReservationCount,
                performancePrices = performancePrices.map { it.domain },
                rounds = rounds.map { it.domain },
            )

    private val PerformanceRoundEntity.domain: PerformanceRound
        get() =
            PerformanceRound(
                id = id,
                roundStartTime = roundStartTime,
                reservationStartTime = reservationStartTime,
                reservationEndTime = reservationEndTime,
            )

    private val PerformancePriceEntity.domain: PerformancePrice
        get() =
            PerformancePrice(
                id = id,
                priceName = priceName,
                price = Money(price),
            )

    private val Performance.entity: PerformanceEntity
        get() =
            PerformanceEntity(
                id = id,
                title = title,
                description = description,
                genre = genre,
                imageUrl = imageUrl,
                location = location,
                maxReservationCount = maxReservationCount,
                performancePrices =
                    performancePrices.map {
                        PerformancePriceEntity(
                            id = it.id,
                            priceName = it.priceName,
                            price = it.price.amount,
                            performanceId = this.id,
                        )
                    },
                rounds =
                    rounds.map {
                        PerformanceRoundEntity(
                            id = it.id,
                            roundStartTime = it.roundStartTime,
                            reservationStartTime = it.reservationStartTime,
                            reservationEndTime = it.reservationEndTime,
                            performanceId = this.id,
                        )
                    },
            )
}
