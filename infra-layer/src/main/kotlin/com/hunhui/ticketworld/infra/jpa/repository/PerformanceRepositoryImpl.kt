package com.hunhui.ticketworld.infra.jpa.repository

import com.hunhui.ticketworld.common.vo.Money
import com.hunhui.ticketworld.domain.performance.Performance
import com.hunhui.ticketworld.domain.performance.PerformanceRepository
import com.hunhui.ticketworld.domain.performance.PerformanceRound
import com.hunhui.ticketworld.domain.performance.SeatGrade
import com.hunhui.ticketworld.infra.jpa.entity.PerformanceEntity
import com.hunhui.ticketworld.infra.jpa.entity.PerformanceRoundEntity
import com.hunhui.ticketworld.infra.jpa.entity.SeatGradeEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
internal class PerformanceRepositoryImpl(
    private val performanceJpaRepository: PerformanceJpaRepository
): PerformanceRepository {
    override fun findById(id: UUID): Performance? = performanceJpaRepository.findByIdOrNull(id)?.domain

    override fun findAll(page: Int, size: Int): List<Performance> = performanceJpaRepository.findAll(Pageable.ofSize(size).withPage(page)).content.map { it.domain }

    override fun save(performance: Performance) {
        performanceJpaRepository.save(performance.entity)
    }

    private val PerformanceEntity.domain: Performance
        get() = Performance(
            id = id,
            title = title,
            description = description,
            genre = genre,
            imageUrl = imageUrl,
            location = location,
            status = status,
            seatGrades = seatGrades.map { it.domain },
            rounds = rounds.map { it.domain }
        )

    private val PerformanceRoundEntity.domain: PerformanceRound
        get() = PerformanceRound(
            id = id,
            performanceDateTime = performanceDateTime,
            reservationStartDateTime = reservationStartDateTime,
            reservationFinishDateTime = reservationFinishDateTime
        )

    private val SeatGradeEntity.domain: SeatGrade
        get() = SeatGrade(
            id = id,
            gradeName = gradeName,
            price = Money(price)
        )

    private val Performance.entity: PerformanceEntity
        get() = PerformanceEntity(
            id = id,
            title = title,
            description = description,
            genre = genre,
            imageUrl = imageUrl,
            location = location,
            status = status,
            seatGrades = seatGrades.map { it.entity },
            rounds = rounds.map { it.entity }
        )

    private val PerformanceRound.entity: PerformanceRoundEntity
        get() = PerformanceRoundEntity(
            id = id,
            performanceDateTime = performanceDateTime,
            reservationStartDateTime = reservationStartDateTime,
            reservationFinishDateTime = reservationFinishDateTime
        )

    private val SeatGrade.entity: SeatGradeEntity
        get() = SeatGradeEntity(
            id = id,
            gradeName = gradeName,
            price = price.amount
        )
}