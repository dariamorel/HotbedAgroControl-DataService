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
        val entity = UserEntity().apply {
            ipAddress = userCreate.ipAddress
            topic = userCreate.topic
            userName = userCreate.userName
            password = userCreate.password
            port = userCreate.port
        }
        val saved = usersRepository.save(entity)
        val userId = saved.id ?: throw IllegalStateException("Saved user has no id")
        connectUser(userId, userCreate)
        log.debug("User added!")
        return ResponseEntity.status(HttpStatus.CREATED).body(
            UserResponse(
                id = saved.id ?: 0L,
                ipAddress = saved.ipAddress,
                topic = saved.topic,
                userName = saved.userName,
                password = saved.password,
                port = saved.port
            )
        )
    }

    @Transactional
    override fun deleteUser(id: Long): ResponseEntity<Unit> {
        val entity = usersRepository.findById(id).orElseThrow { UserNotFoundException(id) }
        dataService.disconnect(id)
        usersRepository.delete(entity)
        log.debug("User deleted!")
        return ResponseEntity.noContent().build()
    }

    fun connectUser(userId: Long, userCreate: UserCreate) {
        dataService.connect(userId, userCreate) { topic, response ->
            mqttMessageHandler.handleMessage(userId, topic, response)
        }
    }
}