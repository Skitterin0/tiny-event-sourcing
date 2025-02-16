package ru.quipy.projections.view

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import ru.quipy.domain.Unique
import java.util.*

class UserView {

	@Document("user-info-view")
	data class UserInfo(
		@Id
		override val id: UUID,
		var nickname: String,
		var userName: String,
		var password: String,
		val projectIds: MutableSet<UUID> = mutableSetOf(),
		val tasksAssignedIds: MutableSet<UUID> = mutableSetOf(),
	): Unique<UUID>
}
