package ru.quipy.projections.service

import ru.quipy.api.*
import ru.quipy.core.EventSourcingService
import ru.quipy.logic.ProjectAggregateState
import ru.quipy.projections.repository.TaskRepository
import ru.quipy.projections.view.TaskView
import ru.quipy.streams.AggregateSubscriptionsManager
import java.util.*
import javax.annotation.PostConstruct

class TaskViewService(
	private val taskRepository: TaskRepository,
	private val subscriptionsManager: AggregateSubscriptionsManager,
	private val projectEsService: EventSourcingService<UUID, ProjectAggregate, ProjectAggregateState>
) {
	@PostConstruct
	fun init() {
		subscriptionsManager.createSubscriber(ProjectAggregate::class, "task-event-stream") {
			`when`(TaskCreatedEvent::class) { event ->
				createTask(event)
			}

			`when`(TaskPerformerSetEvent::class) { event ->
				setPerformer(event)
			}

			`when`(TaskDeletedEvent::class) { event ->
				removeTask(event)
			}

			`when`(TagAssignedToTaskEvent::class) { event ->
				addTag(event)
			}
		}
	}

	private fun createTask(event: TaskCreatedEvent) {
		val defaultTagId = projectEsService.getState(event.projectId)?.defaultTagId

		taskRepository.save(
			TaskView.TaskInfo(
				event.taskId,
				event.projectId,
				event.taskName,
				null,
				defaultTagId,
				Date(System.currentTimeMillis()),
				Date(System.currentTimeMillis())
			)
		)
	}

	private fun setPerformer(event: TaskPerformerSetEvent) {
		val task = taskRepository.findById(event.taskId).orElseThrow()
		task.performerId = event.userId
		taskRepository.save(task)
	}

	private fun removeTask(event: TaskDeletedEvent) {
		taskRepository.deleteById(event.taskId)
	}

	private fun addTag(event: TagAssignedToTaskEvent) {
		val task = taskRepository.findById(event.taskId).orElseThrow()
		task.tagId = event.tagId
		taskRepository.save(task)
	}

	fun findByProjectId(projectId: UUID): List<TaskView.TaskInfo> {
		return taskRepository.findByProjectId(projectId)
	}
	fun findByUserID(userID: UUID): List<TaskView.TaskInfo> {
		return taskRepository.findByUserId(userID)
	}
}
