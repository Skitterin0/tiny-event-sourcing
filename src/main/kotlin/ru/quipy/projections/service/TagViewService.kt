package ru.quipy.projections.service

import ru.quipy.api.ProjectAggregate
import ru.quipy.api.TagCreatedEvent
import ru.quipy.api.TagDeletedEvent
import ru.quipy.projections.repository.TagRepository
import ru.quipy.projections.view.TagView
import ru.quipy.streams.AggregateSubscriptionsManager
import javax.annotation.PostConstruct

class TagViewService(
	private val tagRepository: TagRepository,
	private val subscriptionsManager: AggregateSubscriptionsManager
) {
	@PostConstruct
	fun init() {
		subscriptionsManager.createSubscriber(ProjectAggregate::class, "tag-event-stream") {
			`when`(TagCreatedEvent::class) { event ->
				createTag(event)
			}

			`when`(TagDeletedEvent::class) { event ->
				removeTag(event)
			}
		}
	}

	private fun createTag(event: TagCreatedEvent) {
		tagRepository.save(
			TagView.TagInfo(
				id = event.id,
				projectId = event.projectId,
				tagName = event.tagName,
				tagColor = event.tagColor
			)
		)
	}

	private fun removeTag(event: TagDeletedEvent) {
		tagRepository.deleteById(event.tagId)
	}
}
