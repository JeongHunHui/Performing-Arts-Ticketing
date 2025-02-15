package com.hunhui.ticketworld.infra.jpa.repository

import com.hunhui.ticketworld.infra.jpa.entity.PerformanceEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

internal interface PerformanceJpaRepository : JpaRepository<PerformanceEntity, UUID> {
    fun findByKopisId(kopisId: String): PerformanceEntity?

    @Query(
        """
        SELECT p 
        FROM PerformanceEntity p 
        LEFT JOIN p.rounds r 
        GROUP BY p 
        ORDER BY MIN(r.roundStartTime) ASC
        """,
    )
    fun findAllOrderByEarliestRound(pageable: Pageable): Page<PerformanceEntity>
}
