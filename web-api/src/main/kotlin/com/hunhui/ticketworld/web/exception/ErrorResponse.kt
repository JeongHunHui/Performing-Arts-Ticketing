package com.hunhui.ticketworld.web.exception

import com.hunhui.ticketworld.common.error.ErrorCode
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

class ErrorResponse(
    errorCode: ErrorCode,
    httpStatus: HttpStatus,
) : ResponseEntity<ErrorResponse.Body>(Body(errorCode.code, errorCode.message), httpStatus) {
    data class Body(
        val code: String,
        val message: String,
    )
}
