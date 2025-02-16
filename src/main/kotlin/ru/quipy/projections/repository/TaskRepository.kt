package ru.quipy.projections.repository

import org.springframework.data.mongodb.repository.MongoRepository
import ru.quipy.projections.view.TaskView
import java.util.UUID

interface TaskRepository: MongoRepository<TaskView, UUID> {
}
