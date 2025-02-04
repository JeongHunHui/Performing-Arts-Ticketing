package com.hunhui.ticketworld.web.controller

import com.hunhui.ticketworld.application.UserService
import com.hunhui.ticketworld.application.dto.response.UserCreateResponse
import com.hunhui.ticketworld.web.controller.doc.UserApiDoc
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/user")
class UserController(
    private val userService: UserService,
) : UserApiDoc {
    @PostMapping
    override fun createUser(): ResponseEntity<UserCreateResponse> = ResponseEntity.ok(userService.create())
}
