package com.hunhui.ticketworld.infra.jpa.repository

import com.hunhui.ticketworld.common.error.BusinessException
import com.hunhui.ticketworld.domain.user.User
import com.hunhui.ticketworld.domain.user.UserRepository
import com.hunhui.ticketworld.domain.user.exception.UserErrorCode.NOT_FOUND
import com.hunhui.ticketworld.infra.jpa.entity.UserEntity
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
internal class UserRepositoryImpl(
    private val userJpaRepository: UserJpaRepository,
) : UserRepository {
    override fun getById(id: UUID): User = userJpaRepository.findByIdOrNull(id)?.domain ?: throw BusinessException(NOT_FOUND)

    override fun save(user: User) {
        userJpaRepository.save(user.entity)
    }

    private val UserEntity.domain: User
        get() = User(id = id)

    private val User.entity: UserEntity
        get() = UserEntity(id = id)
}
