package ru.quipy.projections.view

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import ru.quipy.domain.Unique
import java.util.*

class TaskView {

	@Document("task-info-view")
	data class TaskInfo(
		@Id
		override val id: UUID,
		var projectId: UUID,
		var performerId: UUID,
		var tagId: UUID,
		var creationDate: Date,
		var lastUpdatedDate: Date,
	): Unique<UUID>
}
