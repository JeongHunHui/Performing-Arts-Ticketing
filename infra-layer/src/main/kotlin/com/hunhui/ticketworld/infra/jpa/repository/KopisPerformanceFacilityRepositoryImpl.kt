package com.hunhui.ticketworld.infra.jpa.repository

import com.hunhui.ticketworld.domain.kopis.KopisPerformanceFacility
import com.hunhui.ticketworld.infra.http.KopisPerformanceFacilityRepository
import com.hunhui.ticketworld.infra.jpa.entity.KopisPerformanceFacilityEntity
import com.hunhui.ticketworld.infra.jpa.entity.KopisPerformancePlaceEntity
import org.springframework.stereotype.Repository

@Repository
internal class KopisPerformanceFacilityRepositoryImpl(
    private val kopisPerformanceFacilityJpaRepository: KopisPerformanceFacilityJpaRepository,
) : KopisPerformanceFacilityRepository {
    override fun findById(id: String): KopisPerformanceFacility? =
        kopisPerformanceFacilityJpaRepository.findById(id).map { it.domain }.orElse(null)

    override fun save(kopisPerformanceFacility: KopisPerformanceFacility) {
        kopisPerformanceFacilityJpaRepository.save(kopisPerformanceFacility.entity)
    }

    private val KopisPerformanceFacilityEntity.domain: KopisPerformanceFacility
        get() =
            KopisPerformanceFacility(
                id = id,
                name = name,
                address = address,
                places =
                    places.map {
                        KopisPerformanceFacility.Place(
                            id = it.id,
                            name = it.name,
                            seatScale = it.seatScale,
                        )
                    },
            )

    private val KopisPerformanceFacility.entity: KopisPerformanceFacilityEntity
        get() =
            KopisPerformanceFacilityEntity(
                id = id,
                name = name,
                address = address,
                places =
                    places.map {
                        KopisPerformancePlaceEntity(
                            id = it.id,
                            kopisPerformanceFacilityId = id,
                            name = it.name,
                            seatScale = it.seatScale,
                        )
                    },
            )
}
