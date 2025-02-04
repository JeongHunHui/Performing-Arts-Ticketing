package com.hunhui.ticketworld.application

import com.hunhui.ticketworld.application.dto.response.UserCreateResponse
import com.hunhui.ticketworld.domain.user.User
import com.hunhui.ticketworld.domain.user.UserRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    fun create(): UserCreateResponse {
        val user = User(UUID.randomUUID())
        userRepository.save(user)
        return UserCreateResponse(user.id)
    }
}
