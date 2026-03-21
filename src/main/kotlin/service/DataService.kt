package org.example.service

import org.slf4j.LoggerFactory
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.example.model.UserCreate
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap

@Service
class DataService {

    private data class MqttSession(
        val client: MqttClient,
        val userCreate: UserCreate,
        val onMessageReceived: (String, String) -> Unit
    )

    private val sessions = ConcurrentHashMap<Long, MqttSession>()
    private val log = LoggerFactory.getLogger(javaClass)

    fun connect(userId: Long, userCreate: UserCreate, onMessageReceived: (String, String) -> Unit) {
        disconnect(userId)

        val serverUri = "tcp://${userCreate.ipAddress}:${userCreate.port}"
        val options = MqttConnectOptions().apply {
            isAutomaticReconnect = true
            isCleanSession = false
            userName = userCreate.userName
            password = userCreate.password.toCharArray()
        }

        val client = MqttClient(
            serverUri,
            "${MqttClient.generateClientId()}_$userId",
            MemoryPersistence()
        )

        val session = MqttSession(client, userCreate, onMessageReceived)
        sessions[userId] = session

        client.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable?) {
                log.debug("Connection lost!")
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                val topicString = topic ?: return
                val payload = message?.payload ?: return
                val messageString = String(payload, StandardCharsets.UTF_8)
                if (topicString.isBlank() || messageString.isBlank()) return
                onMessageReceived(topicString, messageString)
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {}
        })

        try {
            client.connect(options)
            if (client.isConnected) {
                log.debug("Connected!")
            }
            client.subscribe("${userCreate.topic}/#", 1)
        } catch (e: Exception) {
            log.warn("Connection error: ${e.message}.")
        }
    }

    fun disconnect(userId: Long) {
        sessions.remove(userId)?.let { session ->
            try {
                if (session.client.isConnected) {
                    session.client.disconnect()
                    log.debug("Disconnected!")
                }
                session.client.close()
            } catch (_: Exception) {
            }
        }
    }
}
