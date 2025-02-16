package ru.quipy.projections.service

import ru.quipy.api.UserAggregate
import ru.quipy.api.UserRegisteredEvent
import ru.quipy.projections.repository.ProjectRepository
import ru.quipy.projections.repository.UserRepository
import ru.quipy.projections.view.UserView
import ru.quipy.streams.AggregateSubscriptionsManager
import javax.annotation.PostConstruct

class UserViewService (
	private val projectRepository: ProjectRepository,
	private val userRepository: UserRepository,
	private val subscriptionsManager: AggregateSubscriptionsManager
) {
	@PostConstruct
	fun init() {
		subscriptionsManager.createSubscriber(UserAggregate::class, "user-event-stream") {
			`when`(UserRegisteredEvent::class) { event ->
				register(event)
			}
		}
	}

	private fun register(event: UserRegisteredEvent) {
		userRepository.save(
			UserView.UserInfo(
				id = event.id,
				username = event.username,
				fullName = event.fullName,
				password = event.password,
				projectIds = mutableSetOf(),
				tasksAssignedIds = mutableSetOf()
			)
		)
	}

	fun findUser(username: String): UserView.UserInfo? {
		return userRepository.findByUsername(username)
	}
}
