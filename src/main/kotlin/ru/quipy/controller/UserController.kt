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
import ru.quipy.exceptions.AuthorizeException
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
        if (userService.findByUsername(username) != null) {
            throw RuntimeException("User with such username already exists: $username")
        }

        return userEsService.create { it.register(UUID.randomUUID(), username, fullName, password) }
    }

    @GetMapping("/id")
    fun getUser(@RequestParam searchUser: String, @RequestParam user: String): UserAggregateState? {
        val searchUserId = UUID.fromString(searchUser)
        val userId = UUID.fromString(user)

        if (!userService.userExists(userId)) {
            throw AuthorizeException()
        }
        return userEsService.getState(searchUserId)
    }

    @GetMapping("/username")
    fun findUser(@RequestParam username: String, @RequestParam searchUser: String): UserView.UserInfo? {
        val userId = UUID.fromString(searchUser)

        if (!userService.userExists(userId)) {
            throw AuthorizeException()
        }
        return userService.findByUsername(username)
    }
    @GetMapping("/{searchUser}/tasks")
    fun findTasks(@PathVariable searchUser: String, @RequestParam user: String): List<TaskView.TaskInfo> {
        val searchUserId = UUID.fromString(searchUser)
        val userId = UUID.fromString(user)

        if (!userService.userExists(userId)) {
            throw AuthorizeException()
        }
        return taskService.findByUserID(searchUserId)
    }
}
