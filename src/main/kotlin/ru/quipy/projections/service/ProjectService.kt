package ru.quipy.projections.service

import org.springframework.stereotype.Service
import ru.quipy.api.*
import ru.quipy.core.EventSourcingService
import ru.quipy.logic.ProjectAggregateState
import ru.quipy.projections.repository.ProjectRepository
import ru.quipy.projections.repository.TaskRepository
import ru.quipy.projections.view.ProjectView
import ru.quipy.projections.view.TaskView
import ru.quipy.streams.AggregateSubscriptionsManager
import java.util.*
import javax.annotation.PostConstruct

@Service
class ProjectService(
    private val projectRepository: ProjectRepository,
    private val projectEsService: EventSourcingService<UUID, ProjectAggregate, ProjectAggregateState>,
    private val subscriptionsManager: AggregateSubscriptionsManager,
    private val taskRepository: TaskRepository
) {

    @PostConstruct
    fun init() {
        subscriptionsManager.createSubscriber(ProjectAggregate::class, "project-event-stream") {
            `when`(ProjectCreatedEvent::class) { event ->
                createProject(event)
            }
            `when`(TagCreatedEvent::class) { event ->
                addTag(event)
            }
            `when`(TaskCreatedEvent::class) { event ->
                addTask(event)
            }
            `when`(ParticipantAddedEvent::class) { event ->
                addParticipant(event)
            }
            `when`(ProjectTitleChangedEvent::class) { event ->
                changeTitle(event)
            }
            `when`(TagDeletedEvent::class) { event ->
                deleteTag(event)
            }
        }
    }

    private fun createProject(event: ProjectCreatedEvent) {
        val tagId = projectEsService.getState(event.projectId)?.defaultTagId

        projectRepository.save(
            ProjectView.ProjectInfo(
                    event.projectId,
                    event.title,
                    mutableSetOf<UUID>().apply {
                        tagId?.let { add(it) }
                    },
                    participants = mutableSetOf<UUID>(event.creatorId)
            )
        )
    }

    private fun addTag(event: TagCreatedEvent) {
        val project = projectRepository.findById(event.projectId).orElseThrow()
        project.tags.add(event.tagId)

        projectRepository.save(project)
    }

    private fun addTask(event: TaskCreatedEvent) {
        val project = projectRepository.findById(event.projectId).orElseThrow()

        project.tasks.add(event.taskId)
        projectRepository.save(project)
    }

    private fun addParticipant(event: ParticipantAddedEvent) {
        val project = projectRepository.findById(event.projectId).orElseThrow()

        project.participants.add(event.userId)
        projectRepository.save(project)
    }

    private fun changeTitle(event: ProjectTitleChangedEvent) {
        val project = projectRepository.findById(event.projectId).orElseThrow()

        project.projectTitle = event.title
        projectRepository.save(project)
    }

    private fun deleteTag(event: TagDeletedEvent) {
        val project = projectRepository.findById(event.projectId).orElseThrow()

        project.tags.remove(event.tagId)
        projectRepository.save(project)
    }

    fun getProject(projectId: UUID): ProjectView.ProjectInfo {
        return projectRepository.findById(projectId).orElseThrow()
    }

    fun findByProjectTitle(projectTitle: String): List<ProjectView.ProjectInfo> {
        return projectRepository.findByProjectTitle(projectTitle)
    }

    fun findTasksByTagId(tagId: UUID): List<TaskView.TaskInfo> {
        return taskRepository.findByTagId(tagId)
    }
}
