package ru.quipy.projections.service

import ru.quipy.api.UserRegisteredEvent
import ru.quipy.projections.repository.UserRepository
import ru.quipy.projections.view.UserView

class UserService {
    lateinit var userRepository: UserRepository

    fun addUser(event: UserRegisteredEvent) {
        userRepository.save(
                UserView.UserInfo(
                        event.userId,
                        event.username,
                        event.fullName,
                        event.password,
                )
        )
    }
}