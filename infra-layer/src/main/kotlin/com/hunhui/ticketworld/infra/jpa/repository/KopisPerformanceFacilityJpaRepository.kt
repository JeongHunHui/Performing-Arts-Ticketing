package com.hunhui.ticketworld.infra.jpa.repository

import com.hunhui.ticketworld.infra.jpa.entity.KopisPerformanceFacilityEntity
import org.springframework.data.jpa.repository.JpaRepository

internal interface KopisPerformanceFacilityJpaRepository : JpaRepository<KopisPerformanceFacilityEntity, String>
