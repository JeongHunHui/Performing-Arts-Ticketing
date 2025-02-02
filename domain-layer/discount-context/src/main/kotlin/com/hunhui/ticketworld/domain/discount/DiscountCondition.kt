package com.hunhui.ticketworld.domain.discount

import java.time.LocalDateTime
import java.util.UUID

sealed class DiscountCondition(
    val type: DiscountConditionType,
) {
    abstract val id: UUID

    abstract fun canApply(
        roundId: UUID,
        priceId: UUID,
    ): Boolean

    data class ReservationDate(
        override val id: UUID = UUID.randomUUID(),
        val startDate: LocalDateTime,
        val endDate: LocalDateTime,
    ) : DiscountCondition(DiscountConditionType.RESERVATION_DATE) {
        override fun canApply(
            roundId: UUID,
            priceId: UUID,
        ): Boolean = LocalDateTime.now() in startDate..endDate
    }

    data class RoundIds(
        override val id: UUID = UUID.randomUUID(),
        val roundIds: List<UUID>,
    ) : DiscountCondition(DiscountConditionType.ROUND_ID) {
        override fun canApply(
            roundId: UUID,
            priceId: UUID,
        ): Boolean = roundIds.contains(roundId)
    }

    data class Certificate(
        override val id: UUID = UUID.randomUUID(),
        val message: String,
    ) : DiscountCondition(DiscountConditionType.CERTIFICATE) {
        override fun canApply(
            roundId: UUID,
            priceId: UUID,
        ): Boolean = true
    }

    data class PerformancePriceId(
        override val id: UUID = UUID.randomUUID(),
        val performancePriceId: UUID,
    ) : DiscountCondition(DiscountConditionType.PERFORMANCE_PRICE_ID) {
        override fun canApply(
            roundId: UUID,
            priceId: UUID,
        ): Boolean = priceId == performancePriceId
    }
}
