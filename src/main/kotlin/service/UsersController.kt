package org.example.service

import org.example.api.UsersApi
import org.example.domain.UserEntity
import org.example.model.UserCreate
import org.example.model.UserResponse
import org.example.repository.ElementsRepository
import org.example.repository.UsersRepository
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RestController

@RestController
open class UsersController(
    private val usersRepository: UsersRepository,
    private val elementsRepository: ElementsRepository,
    private val dataService: DataService,
    private val mqttMessageHandler: MqttMessageHandler
): UsersApi {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun addUser(userCreate: UserCreate): ResponseEntity<UserResponse> {
        val newUser = userCreate.copy(
            ipAddress = userCreate.ipAddress.trim(),
            topic = userCreate.topic.trim(),
            userName = userCreate.userName.trim(),
            password = userCreate.password.trim(),
            port = userCreate.port
        )
        val existing = usersRepository.findExistingUser(
            ipAddress = newUser.ipAddress,
            topic = newUser.topic,
            userName = newUser.userName,
            password = newUser.password,
            port = newUser.port
        )
        if (existing != null) {
            log.debug("User already exists, returning existing user")
            return ResponseEntity.ok(existing.toResponse())
        }

        val entity = UserEntity().apply {
            ipAddress = newUser.ipAddress
            topic = newUser.topic
            userName = newUser.userName
            password = newUser.password
            port = newUser.port
        }
        val saved = usersRepository.save(entity)
        val userId = saved.id ?: throw IllegalStateException("Saved user has no id")
        connectUser(userId, newUser)
        log.debug("User added!")
        return ResponseEntity.status(HttpStatus.CREATED).body(saved.toResponse())
    }

    @Transactional
    override fun deleteUser(id: Long): ResponseEntity<Unit> {
        dataService.disconnect(id)
//        val entity = usersRepository.findById(id).orElseThrow { UserNotFoundException(id) }
//        usersRepository.delete(entity)
        log.debug("User with id $id disconnected!")
        return ResponseEntity.noContent().build()
    }

    fun connectUser(userId: Long, userCreate: UserCreate) {
        dataService.connect(userId, userCreate) { topic, response ->
            mqttMessageHandler.handleMessage(userId, topic, response)
        }
    }

    private fun UserEntity.toResponse(): UserResponse = UserResponse(
        id = id ?: 0L,
        ipAddress = ipAddress,
        topic = topic,
        userName = userName,
        password = password,
        port = port
    )
}