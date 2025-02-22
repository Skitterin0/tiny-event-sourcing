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

    @PostMapping("/create")
    fun createProject(@RequestParam projectTitle: String, @RequestParam creator: String): ProjectCreatedEvent {
        val creatorId = UUID.fromString(creator)

        if (!userService.userExists(creatorId)) {
            throw AuthorizeException()
        }
        return projectEsService.create { it.create(UUID.randomUUID(), projectTitle, creatorId) }
    }

    @GetMapping("/id")
    fun getProject(@RequestParam project: String, @RequestParam user: String) : ProjectAggregateState? {
        val projectId = UUID.fromString(project)
        val userId = UUID.fromString(user)

        if (!userService.userExists(userId)) {
            throw AuthorizeException()
        }
        return projectEsService.getState(projectId)
    }

    @PostMapping("/{projectId}/tasks/create")
    fun createTask(
                    @PathVariable project: String,
                    @RequestParam taskName: String,
                    @RequestParam creator: String): TaskCreatedEvent {
        val projectId = UUID.fromString(project)
        val creatorId = UUID.fromString(creator)

        userCheck(projectId, creatorId)

        return projectEsService.update(projectId) {
            it.addTask(taskName, creatorId)
        }
    }

    @PostMapping("/{project}/tags/create")
    fun createTag(
            @PathVariable project: String,
            @RequestParam tagName: String,
            @RequestParam tagColor: String,
            @RequestParam user: String
    ): TagCreatedEvent {
        val projectId = UUID.fromString(project)
        val userId = UUID.fromString(user)

        userCheck(projectId, userId)

        return projectEsService.update(projectId) {
            it.createTag(name = tagName, color = tagColor)
        }
    }

    @PatchMapping("/{project}/tasks/{task}/tags/assign")
    fun assignTagToTask(
            @PathVariable project: String,
            @PathVariable task: String,
            @RequestParam tag: String,
            @RequestParam user: String
    ): TagAssignedToTaskEvent {
        val projectId = UUID.fromString(project)
        val taskId = UUID.fromString(task)
        val tagId = UUID.fromString(tag)
        val userId = UUID.fromString(user)

        userCheck(projectId, userId)

        return projectEsService.update(projectId) {
            it.assignTagToTask(tagId = tagId, taskId = taskId)
        }
    }

    @PatchMapping("/{project}/participants/add")
    fun addParticipant(
            @PathVariable project: String,
            @RequestParam participant: String,
            @RequestParam username: String,
            @RequestParam user: String
    ): ParticipantAddedEvent {
        val projectId = UUID.fromString(project)
        val participantId = UUID.fromString(participant)
        val userId = UUID.fromString(user)

        userCheck(projectId, userId)

        return projectEsService.update(projectId) {
            it.addParticipant(userId = participantId, username = username)
        }
    }

    @PatchMapping("/{project}/tasks/{task}/performer")
    fun setTaskPerformer(
            @PathVariable project: String,
            @PathVariable task: String,
            @RequestParam performer: String,
            @RequestParam user: String
    ): TaskPerformerSetEvent {
        val projectId = UUID.fromString(project)
        val taskId = UUID.fromString(task)
        val performerId = UUID.fromString(performer)
        val userId = UUID.fromString(user)

        userCheck(projectId, userId)

        return projectEsService.update(projectId) {
            it.setTaskPerformer(taskId = taskId, userId = performerId)
        }
    }

    @PatchMapping("/{project}/title")
    fun changeProjectTitle(
            @PathVariable project: String,
            @RequestParam title: String,
            @RequestParam user: String): ProjectTitleChangedEvent {
        val projectId = UUID.fromString(project)
        val userId = UUID.fromString(user)

        userCheck(projectId, userId)

        return projectEsService.update(projectId) {
            it.changeProjectTitle(title = title)
        }
    }

    @DeleteMapping("/{project}/tags/delete")
    fun deleteTag(@PathVariable project: String, @RequestParam tag: String, @RequestParam user: String): TagDeletedEvent {
        val projectId = UUID.fromString(project)
        val userId = UUID.fromString(user)
        val tagId = UUID.fromString(tag)

        userCheck(projectId, userId)

        return projectEsService.update(projectId) {
            it.deleteTag(tagId = tagId)
        }
    }

    @DeleteMapping("/{project}/tasks/delete")
    fun deleteTask(@PathVariable project: String, @RequestParam task: String, @RequestParam user: String): TaskDeletedEvent {
        val projectId = UUID.fromString(project)
        val userId = UUID.fromString(user)
        val taskId = UUID.fromString(task)

        userCheck(projectId, userId)

        return projectEsService.update(projectId) {
            it.deleteTask(taskId = taskId)
        }
    }

    @GetMapping("/title")
    fun findProject(@RequestParam title: String, @RequestParam user: String): List<ProjectView.ProjectInfo> {
        val userId = UUID.fromString(user)

        if (!userService.userExists(userId)) {
            throw AuthorizeException()
        }
        return projectService.findByProjectTitle(title)
    }

    @GetMapping("/{project}/tasks")
    fun getTasks(@PathVariable project: String, @RequestParam user: String): List<TaskView.TaskInfo> {
        val projectId = UUID.fromString(project)
        val userId = UUID.fromString(user)

        userCheck(projectId, userId)

        return taskService.findByProjectId(projectId)
    }

    @GetMapping("/{project}/tags")
    fun getTags(@PathVariable project: String, @RequestParam user: String): List<TagView.TagInfo> {
        val projectId = UUID.fromString(project)
        val userId = UUID.fromString(user)

        userCheck(projectId, userId)

        return tagService.findAllByProjectId(projectId)
    }

    @GetMapping("/{project}/kanban")
    fun getKanban(@PathVariable project: String, @RequestParam user: String): Map<String, List<TaskView.TaskInfo>> {
        val projectId = UUID.fromString(project)
        val userId = UUID.fromString(user)

        userCheck(projectId, userId)

        val tagsMap = tagService.findAllByProjectId(projectId).associate { it.tagName to it.id }

        val kanban = tagsMap.mapValues { (tagName, tagId) ->
            projectService.findTasksByTagId(tagId)
        }

        return kanban
    }

    private fun userCheck(projectId: UUID, userId: UUID) {
        if (!userService.userExists(userId)) {
            throw AuthorizeException()
        }
        if (!projectService.getProject(projectId).participants.contains(userId)) {
            throw ParticipantException()
        }
    }
}
