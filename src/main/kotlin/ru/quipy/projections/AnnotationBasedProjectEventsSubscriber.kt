package ru.quipy.projections

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.quipy.api.*
import ru.quipy.projections.repository.ProjectRepository
import ru.quipy.projections.service.ProjectService
import ru.quipy.streams.annotation.AggregateSubscriber
import ru.quipy.streams.annotation.SubscribeEvent

@Service
@AggregateSubscriber(
    aggregateClass = ProjectAggregate::class, subscriberName = "project-subs-stream"
)
class AnnotationBasedProjectEventsSubscriber {
    lateinit var projectService: ProjectService

    val logger: Logger = LoggerFactory.getLogger(AnnotationBasedProjectEventsSubscriber::class.java)

    @SubscribeEvent
    fun taskCreatedSubscriber(event: TaskCreatedEvent) {
        logger.info("Task created: {}", event.taskName)
        projectService.addTask(event)
    }

    @SubscribeEvent
    fun tagCreatedSubscriber(event: TagCreatedEvent) {
        logger.info("Tag created: {}", event.tagName)
        projectService.addTag(event)
    }

    @SubscribeEvent
    fun projectCreatedSubscriber(event: ProjectCreatedEvent) {
        projectService.createProject(event);
    }

    @SubscribeEvent
    fun participantAddedSubscriber(event: ParticipantAddedEvent) {
        projectService.addParticipant(event)
    }

    @SubscribeEvent
    fun titleChangedSubscriber(event: ProjectTitleChangedEvent) {
        projectService.changeTitle(event)
    }

    @SubscribeEvent
    fun tagDeletedSubscriber(event: TagDeletedEvent) {
        projectService.deleteTag(event)
    }
}