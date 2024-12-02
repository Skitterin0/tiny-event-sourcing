package ru.quipy.api

import ru.quipy.core.annotations.DomainEvent
import ru.quipy.domain.Event
import java.util.*

const val PROJECT_CREATED_EVENT = "PROJECT_CREATED_EVENT"
const val TAG_CREATED_EVENT = "TAG_CREATED_EVENT"
const val TAG_ASSIGNED_TO_TASK_EVENT = "TAG_ASSIGNED_TO_TASK_EVENT"
const val TASK_CREATED_EVENT = "TASK_CREATED_EVENT"
const val PARTICIPANT_ADDED_EVENT = "PARTICIPANT_ADDED_EVENT"
const val TASK_PERFORMER_SET_EVENT = "TASK_PERFORMER_SET_EVENT"
const val TITLE_CHANGED_EVENT = "TITLE_CHANGED_EVENT"
const val TAG_DELETED_EVENT = "TAG_DELETED_EVENT"
const val TASK_DELETED_EVENT = "TASK_DELETED_EVENT"

// API
@DomainEvent(name = PROJECT_CREATED_EVENT)
class ProjectCreatedEvent(
    val projectId: UUID,
    val title: String,
    val creatorId: UUID,
    createdAt: Long = System.currentTimeMillis(),
) : Event<ProjectAggregate>(
    name = PROJECT_CREATED_EVENT,
    createdAt = createdAt,
)

@DomainEvent(name = TAG_CREATED_EVENT)
class TagCreatedEvent(
    val projectId: UUID,
    val tagId: UUID,
    val tagName: String,
    val tagColor: String,
    createdAt: Long = System.currentTimeMillis(),
) : Event<ProjectAggregate>(
    name = TAG_CREATED_EVENT,
    createdAt = createdAt,
)

@DomainEvent(name = TASK_CREATED_EVENT)
class TaskCreatedEvent(
    val projectId: UUID,
    val taskId: UUID,
    val taskName: String,
    createdAt: Long = System.currentTimeMillis(),
) : Event<ProjectAggregate>(
    name = TASK_CREATED_EVENT,
    createdAt = createdAt
)

@DomainEvent(name = TAG_ASSIGNED_TO_TASK_EVENT)
class TagAssignedToTaskEvent(
    val projectId: UUID,
    val taskId: UUID,
    val tagId: UUID,
    createdAt: Long = System.currentTimeMillis(),
) : Event<ProjectAggregate>(
    name = TAG_ASSIGNED_TO_TASK_EVENT,
    createdAt = createdAt
)

@DomainEvent(name = PARTICIPANT_ADDED_EVENT)
class ParticipantAddedEvent(
        val projectId: UUID,
        val userId: UUID,
        val username: String,
        createdAt: Long = System.currentTimeMillis()
) : Event<ProjectAggregate>(
        name = PARTICIPANT_ADDED_EVENT,
        createdAt = createdAt
)

@DomainEvent(name = TASK_PERFORMER_SET_EVENT)
class TaskPerformerSetEvent(
        val projectId: UUID,
        val taskId: UUID,
        val userId: UUID,
        createdAt: Long = System.currentTimeMillis()
) : Event<ProjectAggregate>(
        name = TASK_PERFORMER_SET_EVENT,
        createdAt = createdAt
)

@DomainEvent(name = TITLE_CHANGED_EVENT)
class ProjectTitleChangedEvent(
        val projectId: UUID,
        val title: String,
        createdAt: Long = System.currentTimeMillis()
) : Event<ProjectAggregate>(
        name = TITLE_CHANGED_EVENT,
        createdAt = createdAt
)

@DomainEvent(name = TAG_DELETED_EVENT)
class TagDeletedEvent(
        val projectId: UUID,
        val tagId: UUID,
        createdAt: Long = System.currentTimeMillis()
) : Event<ProjectAggregate>(
        name = TAG_DELETED_EVENT,
        createdAt = createdAt
)

@DomainEvent(name = TASK_DELETED_EVENT)
class TaskDeletedEvent(
        val projectId: UUID,
        val taskId: UUID,
        createdAt: Long = System.currentTimeMillis()
) : Event<ProjectAggregate>(
        name = TASK_DELETED_EVENT,
        createdAt = createdAt
)
