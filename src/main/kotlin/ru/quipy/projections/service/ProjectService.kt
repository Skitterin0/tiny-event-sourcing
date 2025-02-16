package ru.quipy.projections.service

import org.springframework.stereotype.Service
import ru.quipy.api.*
import ru.quipy.core.EventSourcingService
import ru.quipy.logic.ProjectAggregateState
import ru.quipy.projections.repository.ProjectRepository
import ru.quipy.projections.view.ProjectView
import java.util.*

@Service
class ProjectService {
    lateinit var projectRepository: ProjectRepository
    lateinit var projectEsService: EventSourcingService<UUID, ProjectAggregate, ProjectAggregateState>

    fun createProject(event: ProjectCreatedEvent) {
        val tagId = projectEsService.getState(event.projectId)?.defaultTagId

        projectRepository.save(
            ProjectView.ProjectInfo(
                    event.projectId,
                    event.title,
                    mutableSetOf<UUID>().apply {
                        tagId?.let { add(it) }
                    }
            )
        )
    }

    fun addTag(event: TagCreatedEvent) {
        val project = projectRepository.findById(event.projectId).orElseThrow()
        project.tags.add(event.tagId)

        projectRepository.save(project)
    }

    fun addTask(event: TaskCreatedEvent) {
        val project = projectRepository.findById(event.projectId).orElseThrow()

        project.tasks.add(event.taskId)
        projectRepository.save(project)
    }

    fun addParticipant(event: ParticipantAddedEvent) {
        val project = projectRepository.findById(event.projectId).orElseThrow()

        project.participants.add(event.userId)
        projectRepository.save(project)
    }

    fun changeTitle(event: ProjectTitleChangedEvent) {
        val project = projectRepository.findById(event.projectId).orElseThrow()

        project.projectTitle = event.title
        projectRepository.save(project)
    }

    fun deleteTag(event: TagDeletedEvent) {
        val project = projectRepository.findById(event.projectId).orElseThrow()

        project.tags.remove(event.tagId)
        projectRepository.save(project)
    }

    fun getProject(id: UUID): ProjectView.ProjectInfo {
        return projectRepository.findById(id).orElseThrow()
    }
}