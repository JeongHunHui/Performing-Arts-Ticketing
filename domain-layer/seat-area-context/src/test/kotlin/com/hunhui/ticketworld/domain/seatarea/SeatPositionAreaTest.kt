package com.hunhui.ticketworld.domain.seatarea

import com.hunhui.ticketworld.common.error.AssertUtil.assertErrorCode
import com.hunhui.ticketworld.domain.seatarea.exception.SeatAreaErrorCode.SEAT_IS_EMPTY
import com.hunhui.ticketworld.domain.seatarea.exception.SeatAreaErrorCode.SEAT_NOT_CONTAINED
import com.hunhui.ticketworld.domain.seatarea.exception.SeatAreaErrorCode.WIDTH_HEIGHT_IS_NOT_POSITIVE
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class SeatPositionAreaTest {
    @Test
    fun `유효한 SeatArea 생성 성공`() {
        // given
        val seats =
            listOf(
                SeatAreaFixtureFactory.createValidSeatPosition(x = 2, y = 3),
                SeatAreaFixtureFactory.createValidSeatPosition(x = 9, y = 5),
            )
        val seatArea = SeatAreaFixtureFactory.createValidSeatArea(width = 10, height = 10, seatPositions = seats)

        // then
        assertNotNull(seatArea)
        assertEquals(2, seatArea.positions.size)
    }

    @Test
    fun `너비나 높이가 0 이하면 InvalidSeatAreaException 발생`() {
        // when & then
        assertErrorCode(WIDTH_HEIGHT_IS_NOT_POSITIVE) {
            SeatAreaFixtureFactory.createValidSeatArea(width = 0, height = 5)
        }
        assertErrorCode(WIDTH_HEIGHT_IS_NOT_POSITIVE) {
            SeatAreaFixtureFactory.createValidSeatArea(width = 5, height = -1)
        }
    }

    @Test
    fun `좌석이 없으면 InvalidSeatAreaException 발생`() {
        // when & then
        assertErrorCode(SEAT_IS_EMPTY) {
            SeatAreaFixtureFactory.createValidSeatArea(seatPositions = emptyList())
        }
    }

    @Test
    fun `좌석이 영역의 범위를 벗어나면 InvalidSeatAreaException 발생`() {
        // given
        val seats1 =
            listOf(
                SeatAreaFixtureFactory.createValidSeatPosition(x = 2, y = 3),
                SeatAreaFixtureFactory.createValidSeatPosition(x = 10, y = 9),
            )
        val seats2 =
            listOf(
                SeatAreaFixtureFactory.createValidSeatPosition(x = 2, y = 3),
                SeatAreaFixtureFactory.createValidSeatPosition(x = 9, y = 10),
            )

        // when & then
        assertErrorCode(SEAT_NOT_CONTAINED) {
            SeatAreaFixtureFactory.createValidSeatArea(width = 10, height = 10, seatPositions = seats1)
        }
        assertErrorCode(SEAT_NOT_CONTAINED) {
            SeatAreaFixtureFactory.createValidSeatArea(width = 10, height = 10, seatPositions = seats2)
        }
    }
}
