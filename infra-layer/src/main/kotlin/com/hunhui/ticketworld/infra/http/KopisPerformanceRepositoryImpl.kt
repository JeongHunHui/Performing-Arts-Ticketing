package com.hunhui.ticketworld.infra.http

import com.hunhui.ticketworld.domain.kopis.KopisPerformance
import com.hunhui.ticketworld.domain.kopis.KopisPerformanceGenre
import com.hunhui.ticketworld.domain.kopis.KopisPerformanceRepository
import com.hunhui.ticketworld.infra.http.dto.response.KopisPerformanceIdListResponse
import com.hunhui.ticketworld.infra.http.dto.response.KopisPerformanceResponse
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class KopisPerformanceRepositoryImpl(
    private val kopisApiClient: KopisApiClient,
) : KopisPerformanceRepository {
    override fun findIds(
        currentPage: Int,
        rows: Int,
        startDate: LocalDate,
        endDate: LocalDate,
        openRun: Boolean,
        kopisPerformanceGenre: KopisPerformanceGenre,
    ): List<String> =
        kopisApiClient
            .request(
                middleUrl = "/pblprfr",
                queryParams =
                    mapOf(
                        "cpage" to currentPage,
                        "rows" to rows,
                        "stdate" to startDate,
                        "eddate" to endDate,
                        "openrun" to openRun,
                        "shcate" to kopisPerformanceGenre.genreCode,
                    ),
                KopisPerformanceIdListResponse::class.java,
            ).ids
            .map { it.id }

    override fun getById(id: String): KopisPerformance =
        kopisApiClient
            .request(
                middleUrl = "/pblprfr/$id",
                queryParams = emptyMap(),
                KopisPerformanceResponse::class.java,
            ).toDomain()
}
