package com.hunhui.ticketworld.domain.seatarea

import com.hunhui.ticketworld.common.error.AssertUtil.assertErrorCode
import com.hunhui.ticketworld.domain.seatarea.exception.SeatAreaErrorCode.POSITION_IS_NEGATIVE
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class SeatPositionTest {
    @Test
    fun `유효한 좌표로 Seat 생성 성공`() {
        // given
        val seat = SeatAreaFixtureFactory.createValidSeatPosition()

        // then
        assertNotNull(seat)
        assertEquals("A1", seat.number)
    }

    @Test
    fun `x나 y가 음수면 InvalidSeatException 발생`() {
        // when & then
        assertErrorCode(POSITION_IS_NEGATIVE) {
            SeatAreaFixtureFactory.createValidSeatPosition(x = -1, y = 5)
        }
        assertErrorCode(POSITION_IS_NEGATIVE) {
            SeatAreaFixtureFactory.createValidSeatPosition(x = 0, y = -1)
        }
    }
}
