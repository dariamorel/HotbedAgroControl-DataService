package org.example.service

import org.example.model.UserCreate
import org.example.repository.UsersRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class MqttConnectionRestorer(
    private val usersRepository: UsersRepository,
    private val dataService: DataService,
    private val mqttMessageHandler: MqttMessageHandler
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(args: ApplicationArguments?) {
        usersRepository.findAll().forEach { entity ->
            val userId = entity.id ?: return@forEach
            val userCreate = UserCreate(
                ipAddress = entity.ipAddress,
                topic = entity.topic,
                userName = entity.userName,
                password = entity.password,
                port = entity.port
            )
            try {
                dataService.connect(userId, userCreate) { topic, response ->
                    mqttMessageHandler.handleMessage(userId, topic, response)
                }
                log.info("Restored MQTT connection for user id=$userId")
            } catch (e: Exception) {
                log.warn("Failed to restore MQTT for user id=$userId: ${e.message}")
            }
        }
    }
}
