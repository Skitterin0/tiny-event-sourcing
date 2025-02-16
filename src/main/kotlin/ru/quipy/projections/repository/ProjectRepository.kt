package ru.quipy.projections.repository

import org.springframework.data.mongodb.repository.MongoRepository
import ru.quipy.projections.view.ProjectView
import java.util.UUID

interface ProjectRepository: MongoRepository<ProjectView.ProjectInfo, UUID> {
	fun findByProjectTitle(projectTitle: String): List<ProjectView.ProjectInfo>
}
