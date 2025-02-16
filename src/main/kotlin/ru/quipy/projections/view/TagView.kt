package ru.quipy.projections.view

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import ru.quipy.domain.Unique
import java.util.*

class TagView {

	@Document("status-info-view")
	data class TagInfo(
		@Id
		override val id: UUID,
		var projectId: UUID,
		var tagName: String,
		var tagColor: String,
		var orderIndex: Int,
	): Unique<UUID>
}
