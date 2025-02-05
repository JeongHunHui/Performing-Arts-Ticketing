package com.hunhui.ticketworld.infra.jackson.mixin

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.hunhui.ticketworld.domain.seatgrade.DiscountCondition

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonSubTypes(
    JsonSubTypes.Type(DiscountCondition.ReservationDate::class, name = "RESERVATION_DATE"),
    JsonSubTypes.Type(DiscountCondition.PerformanceDate::class, name = "PERFORMANCE_DATE"),
    JsonSubTypes.Type(DiscountCondition.Certificate::class, name = "CERTIFICATE"),
)
interface DiscountConditionMixin
