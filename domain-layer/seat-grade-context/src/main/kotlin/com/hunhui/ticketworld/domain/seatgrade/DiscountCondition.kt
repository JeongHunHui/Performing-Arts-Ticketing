package com.hunhui.ticketworld.domain.seatgrade

import java.time.LocalDateTime
import java.util.UUID

sealed class DiscountCondition(
    val type: DiscountConditionType,
) {
    abstract val id: UUID

    internal abstract fun canApply(): Boolean

    data class ReservationDate(
        override val id: UUID = UUID.randomUUID(),
        val startDate: LocalDateTime,
        val endDate: LocalDateTime,
    ) : DiscountCondition(DiscountConditionType.RESERVATION_DATE) {
        override fun canApply(): Boolean = LocalDateTime.now() in startDate..endDate
    }

    data class PerformanceDate(
        override val id: UUID = UUID.randomUUID(),
        val startDate: LocalDateTime,
        val endDate: LocalDateTime,
    ) : DiscountCondition(DiscountConditionType.PERFORMANCE_DATE) {
        override fun canApply(): Boolean = LocalDateTime.now() in startDate..endDate
    }

    data class Certificate(
        override val id: UUID = UUID.randomUUID(),
        val message: String,
    ) : DiscountCondition(DiscountConditionType.CERTIFICATE) {
        override fun canApply(): Boolean = true
    }
}
