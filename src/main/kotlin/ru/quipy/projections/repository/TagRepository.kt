package ru.quipy.projections.repository

import org.springframework.data.mongodb.repository.MongoRepository
import ru.quipy.projections.view.TagView
import java.util.*

interface TagRepository: MongoRepository<TagView.TagInfo, UUID> {
	fun findAllByProjectId(projectId: UUID): List<TagView.TagInfo>
}
