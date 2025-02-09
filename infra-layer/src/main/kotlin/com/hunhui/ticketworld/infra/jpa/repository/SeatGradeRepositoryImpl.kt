package com.hunhui.ticketworld.infra.jpa.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunhui.ticketworld.common.error.BusinessException
import com.hunhui.ticketworld.common.vo.Money
import com.hunhui.ticketworld.domain.seatgrade.Discount
import com.hunhui.ticketworld.domain.seatgrade.DiscountApplyCount
import com.hunhui.ticketworld.domain.seatgrade.DiscountCondition
import com.hunhui.ticketworld.domain.seatgrade.DiscountRate
import com.hunhui.ticketworld.domain.seatgrade.SeatGrade
import com.hunhui.ticketworld.domain.seatgrade.SeatGradeRepository
import com.hunhui.ticketworld.domain.seatgrade.exception.SeatGradeErrorCode.NOT_FOUND
import com.hunhui.ticketworld.infra.jpa.entity.DiscountConditionEntity
import com.hunhui.ticketworld.infra.jpa.entity.DiscountEntity
import com.hunhui.ticketworld.infra.jpa.entity.SeatGradeEntity
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
internal class SeatGradeRepositoryImpl(
    private val seatGradeJpaRepository: SeatGradeJpaRepository,
    private val objectMapper: ObjectMapper,
) : SeatGradeRepository {
    override fun getById(id: UUID): SeatGrade = seatGradeJpaRepository.findByIdOrNull(id)?.domain ?: throw BusinessException(NOT_FOUND)

    override fun findAllByIds(ids: List<UUID>): List<SeatGrade> = seatGradeJpaRepository.findAllById(ids).map { it.domain }

    override fun findAllByPerformanceId(performanceId: UUID): List<SeatGrade> =
        seatGradeJpaRepository
            .findAllByPerformanceId(performanceId)
            .map { it.domain }

    override fun findAllByPerformanceIds(performanceIds: List<UUID>): Map<UUID, List<SeatGrade>> =
        seatGradeJpaRepository
            .findAllByPerformanceIdIn(performanceIds)
            .groupBy { it.performanceId }
            .mapValues { (_, entities) -> entities.map { it.domain } }

    override fun save(seatGrade: SeatGrade) {
        seatGradeJpaRepository.save(seatGrade.entity)
    }

    override fun saveAll(seatGrades: List<SeatGrade>) {
        seatGradeJpaRepository.saveAll(seatGrades.map { it.entity })
    }

    private val SeatGradeEntity.domain: SeatGrade
        get() =
            SeatGrade(
                id = id,
                performanceId = performanceId,
                name = name,
                price = Money(price),
                discounts = discounts.map { it.domain },
            )

    private val DiscountEntity.domain: Discount
        get() =
            Discount(
                id = id,
                name = name,
                conditions = discountConditions.map { it.domain },
                applyCount = DiscountApplyCount.create(applyCountType, applyCountAmount),
                discountRate = DiscountRate(rate),
            )

    private val DiscountConditionEntity.domain: DiscountCondition
        get() = objectMapper.readValue(this.data, DiscountCondition::class.java)

    private val SeatGrade.entity: SeatGradeEntity
        get() =
            SeatGradeEntity(
                id = id,
                performanceId = performanceId,
                name = name,
                price = price.amount,
                discounts =
                    discounts.map { discount ->
                        DiscountEntity(
                            id = discount.id,
                            seatGradeId = id,
                            name = name,
                            discountConditions =
                                discount.conditions.map {
                                    DiscountConditionEntity(
                                        id = it.id,
                                        discountId = id,
                                        data = it.serialize(),
                                    )
                                },
                            applyCountType = discount.applyCount.type,
                            applyCountAmount = discount.applyCount.amount,
                            rate = discount.discountRate.rate,
                        )
                    },
            )

    private fun DiscountCondition.serialize(): String = objectMapper.writeValueAsString(this)
}
