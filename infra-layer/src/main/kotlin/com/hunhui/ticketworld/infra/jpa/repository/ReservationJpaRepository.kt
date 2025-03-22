package com.hunhui.ticketworld.infra.jpa.repository

import com.hunhui.ticketworld.infra.jpa.entity.ReservationEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

internal interface ReservationJpaRepository : JpaRepository<ReservationEntity, UUID>
