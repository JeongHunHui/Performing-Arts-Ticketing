package com.hunhui.ticketworld.infra.http

import com.hunhui.ticketworld.domain.kopis.KopisPerformanceFacility
import org.springframework.stereotype.Repository

@Repository
interface KopisPerformanceFacilityRepository {
    fun findById(id: String): KopisPerformanceFacility?

    fun save(kopisPerformanceFacility: KopisPerformanceFacility)
}
