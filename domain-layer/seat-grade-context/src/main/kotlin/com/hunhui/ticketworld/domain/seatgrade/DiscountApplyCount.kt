package com.hunhui.ticketworld.domain.seatgrade

sealed class DiscountApplyCount(
    val type: DiscountApplyCountType,
) {
    abstract val amount: Int?

    internal abstract fun canApply(count: Int): Boolean

    companion object {
        fun create(
            type: DiscountApplyCountType,
            amount: Int?,
        ): DiscountApplyCount =
            when (type) {
                DiscountApplyCountType.MAX -> Max(amount!!)
                DiscountApplyCountType.MULTIPLE -> Multiple(amount!!)
                DiscountApplyCountType.INF -> Inf
            }
    }

    /** 최대 {amount}매 적용 가능 */
    data class Max(
        override val amount: Int,
    ) : DiscountApplyCount(DiscountApplyCountType.MAX) {
        override fun canApply(count: Int): Boolean = count <= amount
    }

    /** {amount}의 배수만 적용 가능 */
    data class Multiple(
        override val amount: Int,
    ) : DiscountApplyCount(DiscountApplyCountType.MULTIPLE) {
        override fun canApply(count: Int): Boolean = count % amount == 0
    }

    /** 제한 없음 */
    data object Inf : DiscountApplyCount(DiscountApplyCountType.INF) {
        override val amount = null

        override fun canApply(count: Int): Boolean = true
    }
}
