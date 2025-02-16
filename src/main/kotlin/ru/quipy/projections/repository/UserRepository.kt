package ru.quipy.projections.repository

import org.springframework.data.mongodb.repository.MongoRepository
import ru.quipy.projections.view.UserView
import java.util.*

interface UserRepository: MongoRepository<UserView.UserInfo, UUID> {
	fun findByUsername(username: String): UserView.UserInfo?
}
