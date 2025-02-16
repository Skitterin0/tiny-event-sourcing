package ru.quipy.projections.view

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import ru.quipy.domain.Unique
import java.util.UUID

class ProjectView {

    @Document("project-info-view")
    data class ProjectInfo(
            @Id
            override val id: UUID,
            var projectTitle: String,
            var tasks: MutableSet<UUID> = mutableSetOf(),
            var participants: MutableSet<UUID> = mutableSetOf(),
            var tags: MutableSet<UUID> = mutableSetOf()
    ): Unique<UUID>
}