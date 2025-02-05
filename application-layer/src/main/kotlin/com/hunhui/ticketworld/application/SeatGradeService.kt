package com.hunhui.ticketworld.application

import com.hunhui.ticketworld.application.dto.request.DiscountCreateRequest
import com.hunhui.ticketworld.application.dto.request.DiscountFindRequest
import com.hunhui.ticketworld.application.dto.response.DiscountsBySeatGradeResponse
import com.hunhui.ticketworld.domain.seatgrade.Discount
import com.hunhui.ticketworld.domain.seatgrade.SeatGrade
import com.hunhui.ticketworld.domain.seatgrade.SeatGradeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class SeatGradeService(
    private val seatGradeRepository: SeatGradeRepository,
) {
    @Transactional
    fun addDiscountInSeatGrade(
        seatGradeId: UUID,
        request: DiscountCreateRequest,
    ) {
        val seatGrade: SeatGrade = seatGradeRepository.getById(seatGradeId)
        val discount: Discount = request.toDomain()
        seatGradeRepository.save(seatGrade.addDiscount(discount))
    }

    fun findApplicableDiscountsBySeatGrade(request: DiscountFindRequest): DiscountsBySeatGradeResponse {
        val seatGradeIds: List<UUID> = request.seatGradeIds
        val seatGrades: List<SeatGrade> = seatGradeRepository.findAllByIds(seatGradeIds)
        return DiscountsBySeatGradeResponse.from(seatGrades)
    }
}
