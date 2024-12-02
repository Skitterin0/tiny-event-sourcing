package ru.quipy.logic

import org.apache.el.stream.Stream
import ru.quipy.api.*
import java.util.*


// Commands : takes something -> returns event
// Here the commands are represented by extension functions, but also can be the class member functions

fun ProjectAggregateState.create(id: UUID, title: String, creatorId: UUID): ProjectCreatedEvent {
    return ProjectCreatedEvent(projectId = id, title = title, creatorId = creatorId)
}

fun ProjectAggregateState.addTask(name: String): TaskCreatedEvent {
    return TaskCreatedEvent(projectId = this.getId(), taskId = UUID.randomUUID(), taskName = name)
}

fun ProjectAggregateState.createTag(name: String, color: String): TagCreatedEvent {
    if (projectTags.values.any { it.name == name }) {
        throw IllegalArgumentException("Tag already exists: $name")
    }

    return TagCreatedEvent(projectId = this.getId(), tagId = UUID.randomUUID(), tagName = name, tagColor = color)
}

fun ProjectAggregateState.assignTagToTask(tagId: UUID, taskId: UUID): TagAssignedToTaskEvent {
    if (!projectTags.containsKey(tagId)) {
        throw IllegalArgumentException("Tag doesn't exists: $tagId")
    }

    if (!tasks.containsKey(taskId)) {
        throw IllegalArgumentException("Task doesn't exists: $taskId")
    }

    return TagAssignedToTaskEvent(projectId = this.getId(), tagId = tagId, taskId = taskId)
}

fun ProjectAggregateState.addParticipant(userId: UUID, username: String): ParticipantAddedEvent {
    if (participants.containsKey(userId)) {
        throw IllegalArgumentException("User is already a participant of the project: $userId")
    }

    return ParticipantAddedEvent(projectId = this.getId(), userId = userId, username = username)
}

fun ProjectAggregateState.setTaskPerformer(taskId: UUID, userId: UUID): TaskPerformerSetEvent {
    if (!tasks.containsKey(taskId)) {
        throw IllegalArgumentException("No such task in the project: $taskId")
    }

    if (!participants.containsKey(userId)) {
        throw IllegalArgumentException("User isn't a participant of the project: $userId")
    }

    return TaskPerformerSetEvent(projectId = this.getId(), userId = userId, taskId = taskId)
}

fun ProjectAggregateState.changeProjectTitle(title: String): ProjectTitleChangedEvent {
    if (title.isBlank()) {
        throw IllegalArgumentException("Project title can't be empty")
    }

    return  ProjectTitleChangedEvent(projectId = this.getId(), title = title)
}

fun ProjectAggregateState.deleteTag(tagId: UUID): TagDeletedEvent {
    if (!projectTags.containsKey(tagId)) {
        throw IllegalArgumentException("No such tag in the project: $tagId")
    }

    if (tagId == this.defaultTagId) {
        throw IllegalArgumentException("Can't delete default tag: $tagId")
    }

    if (this.tasks.values.any { it.tag == tagId }) {
        throw IllegalArgumentException("Can't delete tag that is assigned to a task: $tagId")
    }

    return TagDeletedEvent(projectId = this.getId(), tagId = tagId)
}

fun ProjectAggregateState.deleteTask(taskId: UUID): TaskDeletedEvent {
    if (!tasks.containsKey(taskId)) {
        throw IllegalArgumentException("No such task in the project: $taskId")
    }

    return TaskDeletedEvent(projectId = this.getId(), taskId = taskId)
}