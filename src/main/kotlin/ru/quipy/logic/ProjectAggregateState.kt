package ru.quipy.logic

import ru.quipy.api.*
import ru.quipy.core.annotations.StateTransitionFunc
import ru.quipy.domain.AggregateState
import java.util.*

const val DEFAULT_TAG_NAME = "Created"
const val DEFAULT_TAG_COLOR = "#ef2020"

// Service's business logic
class ProjectAggregateState : AggregateState<UUID, ProjectAggregate> {
    private lateinit var projectId: UUID
    val createdAt: Long = System.currentTimeMillis()
    var updatedAt: Long = System.currentTimeMillis()

    lateinit var projectTitle: String
    lateinit var creatorId: UUID
    lateinit var defaultTagId: UUID
    val tasks = mutableMapOf<UUID, TaskEntity>()
    val participants = mutableMapOf<UUID, ParticipantEntity>()
    val projectTags = mutableMapOf<UUID, TagEntity>()

    override fun getId() = projectId

    // State transition functions which is represented by the class member function
    @StateTransitionFunc
    fun projectCreatedApply(event: ProjectCreatedEvent) {
        projectId = event.projectId
        projectTitle = event.title
        creatorId = event.creatorId
        updatedAt = createdAt

        val tagEvent = createTag(DEFAULT_TAG_NAME, DEFAULT_TAG_COLOR)
        defaultTagId = tagEvent.tagId

        tagCreatedApply(tagEvent)
    }

    @StateTransitionFunc
    fun tagCreatedApply(event: TagCreatedEvent) {
        projectTags[event.tagId] = TagEntity(event.tagId, event.tagName, event.tagColor)
        updatedAt = event.createdAt
    }

    @StateTransitionFunc
    fun taskCreatedApply(event: TaskCreatedEvent) {
        tasks[event.taskId] = TaskEntity(event.taskId, event.taskName, defaultTagId, null)
        updatedAt = event.createdAt
    }

    @StateTransitionFunc
    fun participantAddedApply(event: ParticipantAddedEvent) {
        participants[event.userId] = ParticipantEntity(event.userId, event.username)
    }

    @StateTransitionFunc
    fun taskPerformerSetApply(event: TaskPerformerSetEvent) {
        tasks.getValue(event.taskId).performer = event.userId
        tasks.getValue(event.taskId).updatedAt = event.createdAt
        updatedAt = event.createdAt
    }

    @StateTransitionFunc
    fun projectTitleChangedApply(event: ProjectTitleChangedEvent) {
        projectTitle = event.title
        updatedAt = event.createdAt
    }

    @StateTransitionFunc
    fun tagDeletedApply(event: TagDeletedEvent) {
        projectTags.remove(event.tagId)
        updatedAt = event.createdAt
    }

    @StateTransitionFunc
    fun taskDeletedApply(event: TaskDeletedEvent) {
        tasks.remove(event.taskId)
        updatedAt = event.createdAt
    }
}

data class TaskEntity(
    val id: UUID = UUID.randomUUID(),
    var name: String,
    var tag: UUID,
    var performer: UUID?,
    val createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
)

data class ParticipantEntity(
    val id: UUID,
    val username: String
)

data class TagEntity(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val color: String
)

/**
 * Demonstrates that the transition functions might be representer by "extension" functions, not only class members functions
 */
@StateTransitionFunc
fun ProjectAggregateState.tagAssignedApply(event: TagAssignedToTaskEvent) {
    tasks[event.taskId]?.tag = event.tagId
    updatedAt = event.createdAt
}
