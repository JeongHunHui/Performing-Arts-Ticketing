package com.hunhui.ticketworld.infra.http

import com.hunhui.ticketworld.domain.kopis.KopisPerformance
import com.hunhui.ticketworld.domain.kopis.KopisPerformanceFacility
import com.hunhui.ticketworld.domain.kopis.KopisPerformanceGenre
import com.hunhui.ticketworld.domain.kopis.KopisRepository
import com.hunhui.ticketworld.infra.http.dto.response.KopisPerformanceFacilityResponse
import com.hunhui.ticketworld.infra.http.dto.response.KopisPerformanceIdListResponse
import com.hunhui.ticketworld.infra.http.dto.response.KopisPerformanceResponse
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class KopisRepositoryImpl(
    private val kopisApiClient: KopisApiClient,
    private val kopisPerformanceFacilityRepository: KopisPerformanceFacilityRepository,
) : KopisRepository {
    override fun findPerformanceIds(
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
            ?.map { it.id } ?: emptyList()

    override fun getPerformanceById(id: String): KopisPerformance =
        kopisApiClient
            .request(
                middleUrl = "/pblprfr/$id",
                queryParams = emptyMap(),
                KopisPerformanceResponse::class.java,
            ).toDomain()

    override fun getPerformanceFacilityById(id: String): KopisPerformanceFacility {
        val kopisPerformanceFacility = kopisPerformanceFacilityRepository.findById(id)
        return if (kopisPerformanceFacility != null) {
            kopisPerformanceFacility
        } else {
            val facility = fetchPerformanceFacility(id)
            try {
                kopisPerformanceFacilityRepository.save(facility)
            } catch (e: DataIntegrityViolationException) {
                // ignore
            }
            facility
        }
    }

    private fun fetchPerformanceFacility(id: String): KopisPerformanceFacility =
        kopisApiClient
            .request(
                middleUrl = "/prfplc/$id",
                queryParams = emptyMap(),
                KopisPerformanceFacilityResponse::class.java,
            ).toDomain()
}
