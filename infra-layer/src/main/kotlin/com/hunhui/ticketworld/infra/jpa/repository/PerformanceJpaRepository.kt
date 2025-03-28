package com.hunhui.ticketworld.infra.jpa.repository

import com.hunhui.ticketworld.domain.performance.PerformanceSummary
import com.hunhui.ticketworld.infra.jpa.entity.PerformanceEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

internal interface PerformanceJpaRepository : JpaRepository<PerformanceEntity, UUID> {
    @Query(
        """
        SELECT p 
        FROM PerformanceEntity p 
        LEFT JOIN FETCH p.rounds r 
        WHERE p.id = :id
        """,
    )
    fun findByIdOrNull(id: UUID): PerformanceEntity?

    fun findByKopisId(kopisId: String): PerformanceEntity?

    @Query(
        """
        SELECT DISTINCT p
        FROM PerformanceEntity p 
        LEFT JOIN FETCH p.rounds r
        ORDER BY p.startDate ASC
        """,
    )
    fun findAllOrderByEarliestRound(pageable: Pageable): Page<PerformanceEntity>

    @Query(
        """
        SELECT new com.hunhui.ticketworld.domain.performance.PerformanceSummary(
            p.id,
            p.title,
            p.genre,
            p.startDate,
            p.finishDate,
            p.posterUrl,
            p.location
        )
        FROM PerformanceEntity p
        """,
    )
    fun findAllPerformanceSummaries(pageable: Pageable): Page<PerformanceSummary>

    @Query(
        """
        SELECT new com.hunhui.ticketworld.domain.performance.PerformanceSummary(
            p.id,
            p.title,
            p.genre,
            p.startDate,
            p.finishDate,
            p.posterUrl,
            p.location
        )
        FROM PerformanceEntity p
        WHERE p.id IN :ids
        """,
    )
    fun findAllPerformanceSummariesByIds(ids: List<UUID>): List<PerformanceSummary>

    @Query(
        """
        SELECT p 
        FROM PerformanceEntity p 
        LEFT JOIN FETCH p.rounds r 
        WHERE p.id = :performanceId AND r.id = :roundId
        """,
    )
    fun findByIdAndRoundId(
        performanceId: UUID,
        roundId: UUID,
    ): PerformanceEntity?
}
