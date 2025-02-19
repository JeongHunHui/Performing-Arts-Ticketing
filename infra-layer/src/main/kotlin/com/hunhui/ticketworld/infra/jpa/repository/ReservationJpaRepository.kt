package com.hunhui.ticketworld.infra.jpa.repository

import com.hunhui.ticketworld.infra.jpa.entity.ReservationEntity
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import java.util.UUID

internal interface ReservationJpaRepository : JpaRepository<ReservationEntity, UUID> {
    @Query(
        """
        SELECT r
        FROM ReservationEntity r
        WHERE r.id = :id
        """,
    )
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findByIdWithPessimistic(id: UUID): ReservationEntity?
}
