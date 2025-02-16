package ru.quipy.controller

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.quipy.api.*
import ru.quipy.core.EventSourcingService
import ru.quipy.logic.*
import java.util.*

@RestController
@RequestMapping("/projects")
class ProjectController(
    val projectEsService: EventSourcingService<UUID, ProjectAggregate, ProjectAggregateState>
) {

    @PostMapping("/{projectTitle}")
    fun createProject(@PathVariable projectTitle: String, @RequestParam creatorId: UUID): ProjectCreatedEvent {
        return projectEsService.create { it.create(UUID.randomUUID(), projectTitle, creatorId) }
    }

    @GetMapping("/{projectId}")
    fun getProject(@PathVariable projectId: UUID) : ProjectAggregateState? {
        return projectEsService.getState(projectId)
    }

    @PostMapping("/{projectId}/tasks/create")
    fun createTask(@PathVariable projectId: UUID, @RequestParam taskName: String): TaskCreatedEvent {
        return projectEsService.update(projectId) {
            it.addTask(taskName)
        }
    }

    @PostMapping("/{projectId}/tags/create")
    fun createTag(
            @PathVariable projectId: UUID,
            @RequestParam tagName: String,
            @RequestParam tagColor: String
    ): TagCreatedEvent {
        return projectEsService.update(projectId) {
            it.createTag(name = tagName, color = tagColor)
        }
    }

    @PatchMapping("/{projectId}/tasks/{taskId}/tags/assign")
    fun assignTagToTask(
            @PathVariable projectId: UUID,
            @PathVariable taskId: UUID,
            @RequestParam tagId: UUID
    ): TagAssignedToTaskEvent {
        return projectEsService.update(projectId) {
            it.assignTagToTask(tagId = tagId, taskId = taskId)
        }
    }

    @PatchMapping("/{projectId}/participants/add")
    fun addParticipant(
            @PathVariable projectId: UUID,
            @RequestParam userId: UUID,
            @RequestParam username: String
    ): ParticipantAddedEvent {
        return projectEsService.update(projectId) {
            it.addParticipant(userId = userId, username = username)
        }
    }

    @PatchMapping("/{projectId}/tasks/{taskId}/performer")
    fun setTaskPerformer(
            @PathVariable projectId: UUID,
            @PathVariable taskId: UUID,
            @RequestParam userId: UUID
    ): TaskPerformerSetEvent {
        return projectEsService.update(projectId) {
            it.setTaskPerformer(taskId = taskId, userId = userId)
        }
    }

    @PatchMapping("/{projectId}/title")
    fun changeProjectTitle(@PathVariable projectId: UUID, @RequestParam title: String): ProjectTitleChangedEvent {
        return projectEsService.update(projectId) {
            it.changeProjectTitle(title = title)
        }
    }

    @DeleteMapping("/{projectId}/tags/delete")
    fun deleteTag(@PathVariable projectId: UUID, @RequestParam tagId: UUID): TagDeletedEvent {
        return projectEsService.update(projectId) {
            it.deleteTag(tagId = tagId)
        }
    }

    @DeleteMapping("/{projectId}/tasks/delete")
    fun deleteTask(@PathVariable projectId: UUID, @RequestParam taskId: UUID): TaskDeletedEvent {
        return projectEsService.update(projectId) {
            it.deleteTask(taskId = taskId)
        }
    }
}