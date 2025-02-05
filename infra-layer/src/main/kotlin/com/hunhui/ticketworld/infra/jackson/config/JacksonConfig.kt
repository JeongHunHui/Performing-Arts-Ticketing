package com.hunhui.ticketworld.infra.jackson.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.hunhui.ticketworld.domain.seatgrade.DiscountCondition
import com.hunhui.ticketworld.infra.jackson.mixin.DiscountConditionMixin
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JacksonConfig {
    @Bean
    fun objectMapper(): ObjectMapper =
        ObjectMapper()
            .registerModule(JavaTimeModule())
            .registerKotlinModule()
            .addMixIn(DiscountCondition::class.java, DiscountConditionMixin::class.java)
}
