package ru.quipy.projections.repository

import org.springframework.data.mongodb.repository.MongoRepository
import ru.quipy.projections.view.TaskView
import java.util.*

interface TaskRepository: MongoRepository<TaskView.TaskInfo, UUID> {
	fun findByProjectId(projectId: UUID): List<TaskView.TaskInfo>

	fun findByUserId(userId: UUID): List<TaskView.TaskInfo>


}
