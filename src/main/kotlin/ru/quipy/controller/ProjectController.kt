package ru.quipy.controller

import org.springframework.beans.factory.annotation.Autowired
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
import ru.quipy.exceptions.AuthorizeException
import ru.quipy.exceptions.ParticipantException
import ru.quipy.logic.*
import ru.quipy.projections.service.ProjectService
import ru.quipy.projections.service.TagViewService
import ru.quipy.projections.service.TaskViewService
import ru.quipy.projections.service.UserViewService
import ru.quipy.projections.view.ProjectView
import ru.quipy.projections.view.TagView
import ru.quipy.projections.view.TaskView
import java.util.*

@RestController
@RequestMapping("/projects")
class ProjectController(
    val projectEsService: EventSourcingService<UUID, ProjectAggregate, ProjectAggregateState>
) {
    @Autowired
    lateinit var projectService: ProjectService
    @Autowired
    lateinit var userService: UserViewService
    @Autowired
    lateinit var taskService: TaskViewService
    @Autowired
    lateinit var tagService: TagViewService
    val defaultTagName = "Created"

    @PostMapping("/{projectTitle}")
    fun createProject(@PathVariable projectTitle: String, @RequestParam creatorId: UUID): ProjectCreatedEvent {
        if (!userService.userExists(creatorId)) {
            throw AuthorizeException()
        }
        return projectEsService.create { it.create(UUID.randomUUID(), projectTitle, creatorId) }
    }

    @GetMapping("/{projectId}")
    fun getProject(@PathVariable projectId: UUID, @RequestParam userId: UUID) : ProjectAggregateState? {
        if (!userService.userExists(userId)) {
            throw AuthorizeException()
        }
        return projectEsService.getState(projectId)
    }

    @PostMapping("/{projectId}/tasks/create")
    fun createTask(
                    @PathVariable projectId: UUID,
                    @RequestParam taskName: String,
                    @RequestParam creatorId: UUID): TaskCreatedEvent {
        if (!userService.userExists(creatorId)) {
            throw AuthorizeException()
        }
        if (!projectService.getProject(projectId).participants.contains(creatorId)) {
            throw ParticipantException()
        }
        return projectEsService.update(projectId) {
            it.addTask(taskName, creatorId)
        }
    }

    @PostMapping("/{projectId}/tags/create")
    fun createTag(
            @PathVariable projectId: UUID,
            @RequestParam tagName: String,
            @RequestParam tagColor: String,
            @RequestParam userId: UUID
    ): TagCreatedEvent {
        if (!userService.userExists(userId)) {
            throw AuthorizeException()
        }
        if (!projectService.getProject(projectId).participants.contains(userId)) {
            throw ParticipantException()
        }
        return projectEsService.update(projectId) {
            it.createTag(name = tagName, color = tagColor)
        }
    }

    @PatchMapping("/{projectId}/tasks/{taskId}/tags/assign")
    fun assignTagToTask(
            @PathVariable projectId: UUID,
            @PathVariable taskId: UUID,
            @RequestParam tagId: UUID,
            @RequestParam userId: UUID
    ): TagAssignedToTaskEvent {
        if (!userService.userExists(userId)) {
            throw AuthorizeException()
        }
        if (!projectService.getProject(projectId).participants.contains(userId)) {
            throw ParticipantException()
        }
        if (!tagService.findAllByProjectId(projectId).map { tag -> tag.id }.contains(tagId)) {
            throw RuntimeException("Can't assign tag: $tagId that doesn't exist in the project: $projectId")
        }
        return projectEsService.update(projectId) {
            it.assignTagToTask(tagId = tagId, taskId = taskId)
        }
    }

    @PatchMapping("/{projectId}/participants/add")
    fun addParticipant(
            @PathVariable projectId: UUID,
            @RequestParam participantId: UUID,
            @RequestParam username: String,
            @RequestParam userId: UUID
    ): ParticipantAddedEvent {
        if (!userService.userExists(userId)) {
            throw AuthorizeException()
        }
        if (!projectService.getProject(projectId).participants.contains(userId)) {
            throw ParticipantException()
        }
        return projectEsService.update(projectId) {
            it.addParticipant(userId = participantId, username = username)
        }
    }

    @PatchMapping("/{projectId}/tasks/{taskId}/performer")
    fun setTaskPerformer(
            @PathVariable projectId: UUID,
            @PathVariable taskId: UUID,
            @RequestParam performerId: UUID,
            @RequestParam userId: UUID
    ): TaskPerformerSetEvent {
        if (!userService.userExists(userId)) {
            throw AuthorizeException()
        }
        if (!projectService.getProject(projectId).participants.contains(userId)) {
            throw ParticipantException()
        }
        return projectEsService.update(projectId) {
            it.setTaskPerformer(taskId = taskId, userId = performerId)
        }
    }

    @PatchMapping("/{projectId}/title")
    fun changeProjectTitle(
            @PathVariable projectId: UUID,
            @RequestParam title: String,
            @RequestParam userId: UUID): ProjectTitleChangedEvent {
        if (!userService.userExists(userId)) {
            throw AuthorizeException()
        }
        if (!projectService.getProject(projectId).participants.contains(userId)) {
            throw ParticipantException()
        }
        return projectEsService.update(projectId) {
            it.changeProjectTitle(title = title)
        }
    }

    @DeleteMapping("/{projectId}/tags/delete")
    fun deleteTag(@PathVariable projectId: UUID, @RequestParam tagId: UUID, userId: UUID): TagDeletedEvent {
        if (!userService.userExists(userId)) {
            throw AuthorizeException()
        }
        if (!projectService.getProject(projectId).participants.contains(userId)) {
            throw ParticipantException()
        }
        if (projectService.findTasksByTagId(tagId).isNotEmpty()) {
            throw RuntimeException("Can't delete assigned tag with tagId=$tagId")
        }
        if (tagService.getTagById(tagId).tagName == defaultTagName) {
            throw RuntimeException("Can't delete default project tag: tagId=$tagId")
        }

        return projectEsService.update(projectId) {
            it.deleteTag(tagId = tagId)
        }
    }

    @DeleteMapping("/{projectId}/tasks/delete")
    fun deleteTask(@PathVariable projectId: UUID, @RequestParam taskId: UUID, userId: UUID): TaskDeletedEvent {
        if (!userService.userExists(userId)) {
            throw AuthorizeException()
        }
        if (!projectService.getProject(projectId).participants.contains(userId)) {
            throw ParticipantException()
        }
        return projectEsService.update(projectId) {
            it.deleteTask(taskId = taskId)
        }
    }

    @GetMapping("/{title}")
    fun findProject(@PathVariable title: String, userId: UUID): List<ProjectView.ProjectInfo> {
        if (!userService.userExists(userId)) {
            throw AuthorizeException()
        }
        return projectService.findByProjectTitle(title)
    }

    @GetMapping("/{projectId}/tasks")
    fun getTasks(@PathVariable projectId: UUID, userId: UUID): List<TaskView.TaskInfo> {
        if (!userService.userExists(userId)) {
            throw AuthorizeException()
        }
        if (!projectService.getProject(projectId).participants.contains(userId)) {
            throw ParticipantException()
        }
        return taskService.findByProjectId(projectId)
    }

    @GetMapping("/{projectId}/tags")
    fun getTags(@PathVariable projectId: UUID, userId: UUID): List<TagView.TagInfo> {
        if (!userService.userExists(userId)) {
            throw AuthorizeException()
        }
        if (!projectService.getProject(projectId).participants.contains(userId)) {
            throw ParticipantException()
        }
        return tagService.findAllByProjectId(projectId)
    }
}
