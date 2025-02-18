package com.hunhui.ticketworld.infra.jpa.repository

import com.hunhui.ticketworld.common.error.BusinessException
import com.hunhui.ticketworld.domain.performance.Performance
import com.hunhui.ticketworld.domain.performance.PerformanceInfo
import com.hunhui.ticketworld.domain.performance.PerformanceRepository
import com.hunhui.ticketworld.domain.performance.PerformanceRound
import com.hunhui.ticketworld.domain.performance.exception.PerformanceErrorCode.NOT_FOUND
import com.hunhui.ticketworld.infra.jpa.entity.PerformanceEntity
import com.hunhui.ticketworld.infra.jpa.entity.PerformanceRoundEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
internal class PerformanceRepositoryImpl(
    private val performanceJpaRepository: PerformanceJpaRepository,
) : PerformanceRepository {
    override fun getById(id: UUID): Performance = performanceJpaRepository.findByIdOrNull(id)?.domain ?: throw BusinessException(NOT_FOUND)

    override fun getByIdAndRoundId(
        performanceId: UUID,
        roundId: UUID,
    ): Performance = performanceJpaRepository.findByIdAndRoundId(performanceId, roundId)?.domain ?: throw BusinessException(NOT_FOUND)

    override fun findByKopisId(kopisId: String): Performance? = performanceJpaRepository.findByKopisId(kopisId)?.domain

    override fun findAllWithPagenation(
        page: Int,
        size: Int,
    ): Pair<List<Performance>, Int> {
        val performancesWithPage: Page<PerformanceEntity> =
            performanceJpaRepository.findAllOrderByEarliestRound(Pageable.ofSize(size).withPage(page))
        return performancesWithPage.content.map { it.domain } to performancesWithPage.totalPages
    }

    override fun save(performance: Performance) {
        performanceJpaRepository.save(performance.entity)
    }

    private val PerformanceEntity.domain: Performance
        get() =
            Performance(
                id = id,
                info =
                    PerformanceInfo(
                        kopisId = kopisId,
                        title = title,
                        genre = genre,
                        posterUrl = posterUrl,
                        kopisFacilityId = kopisFacilityId,
                        location = location,
                        locationAddress = locationAddress,
                        cast = cast,
                        crew = crew,
                        runtime = runtime,
                        ageLimit = ageLimit,
                        descriptionImageUrls = descriptionImageUrls.split('|'),
                        description = description,
                    ),
                maxReservationCount = maxReservationCount,
                rounds = rounds.map { it.domain },
            )

    private val PerformanceRoundEntity.domain: PerformanceRound
        get() =
            PerformanceRound(
                id = id,
                roundStartTime = roundStartTime,
                reservationStartTime = reservationStartTime,
                reservationEndTime = reservationEndTime,
                isTicketCreated = isTicketCreated,
            )

    private val Performance.entity: PerformanceEntity
        get() =
            PerformanceEntity(
                id = id,
                title = info.title,
                genre = info.genre,
                posterUrl = info.posterUrl,
                location = info.location,
                kopisId = info.kopisId,
                kopisFacilityId = info.kopisFacilityId,
                locationAddress = info.locationAddress,
                cast = info.cast,
                crew = info.crew,
                runtime = info.runtime,
                ageLimit = info.ageLimit,
                descriptionImageUrls = info.descriptionImageUrls.joinToString("|"),
                description = info.description,
                maxReservationCount = maxReservationCount,
                rounds =
                    rounds.map {
                        PerformanceRoundEntity(
                            id = it.id,
                            roundStartTime = it.roundStartTime,
                            reservationStartTime = it.reservationStartTime,
                            reservationEndTime = it.reservationEndTime,
                            performanceId = this.id,
                            isTicketCreated = it.isTicketCreated,
                        )
                    },
            )
}
