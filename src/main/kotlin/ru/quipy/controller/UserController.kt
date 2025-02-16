package ru.quipy.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.quipy.api.UserAggregate
import ru.quipy.api.UserRegisteredEvent
import ru.quipy.core.EventSourcingService
import ru.quipy.logic.UserAggregateState
import ru.quipy.logic.register
import ru.quipy.projections.service.TaskViewService
import ru.quipy.projections.service.UserViewService
import ru.quipy.projections.view.TaskView
import ru.quipy.projections.view.UserView
import java.util.UUID

@RestController
@RequestMapping("/users")
class UserController(
        val userEsService: EventSourcingService<UUID, UserAggregate, UserAggregateState>
) {
    @Autowired
    lateinit var userService: UserViewService
    @Autowired
    lateinit var taskService: TaskViewService

    @PostMapping("/register")
    fun registeredUser(
            @RequestParam username: String,
            @RequestParam fullName: String,
            @RequestParam password: String) : UserRegisteredEvent {
        return userEsService.create { it.register(UUID.randomUUID(), username, fullName, password) }
    }

    @GetMapping("/{userId}")
    fun getUser(@PathVariable userId: UUID): UserAggregateState? {
        return userEsService.getState(userId)
    }

    @GetMapping("/{username}")
    fun findUser(@PathVariable username: String): UserView.UserInfo? {
        return userService.findUserByUsername(username)
    }
    @GetMapping("/{userId}/tasks")
    fun findTasks(@PathVariable userId: UUID): List<TaskView.TaskInfo> {
        return taskService.findByUserID(userId)
    }
}