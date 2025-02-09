package com.hunhui.ticketworld.infra.jpa.entity

import com.hunhui.ticketworld.domain.seatgrade.DiscountApplyCountType
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(name = "discount")
internal class DiscountEntity(
    @Id
    @Column(name = "id", nullable = false)
    val id: UUID,
    @Column(name = "seat_grade_id", nullable = false)
    val seatGradeId: UUID,
    @Column(name = "name", nullable = false)
    val name: String,
    @OneToMany(
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.EAGER,
    )
    @JoinColumn(name = "discount_id")
    val discountConditions: List<DiscountConditionEntity> = listOf(),
    @Enumerated(EnumType.STRING)
    @Column(name = "apply_count_type", nullable = false)
    val applyCountType: DiscountApplyCountType,
    @Column(name = "apply_count_amount")
    val applyCountAmount: Int?,
    @Column(name = "rate", nullable = false)
    val rate: BigDecimal,
) : BaseTimeEntity()
