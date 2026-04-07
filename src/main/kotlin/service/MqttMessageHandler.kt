package org.example.service

import org.example.domain.ElementEntity
import org.example.model.Element
import org.example.repository.ElementsRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class MqttMessageHandler(
    private val elementsRepository: ElementsRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val lastProcessedPerUserAndElement = ConcurrentHashMap<String, Long>()

    companion object {
        private const val MIN_INTERVAL_MS = 60_000L
    }

    fun handleMessage(userId: Long, topicString: String, responseString: String) {
        val definedElement = defineElement(topicString) ?: return
        log.debug("Got element: $definedElement")
        val key = "${userId}_${definedElement.name}"
        val now = System.currentTimeMillis()
        val last = lastProcessedPerUserAndElement.getOrDefault(key, 0L)
        if (now - last < MIN_INTERVAL_MS) return
        lastProcessedPerUserAndElement[key] = now
        try {
            elementsRepository.save(
                ElementEntity().apply {
                    this.userId = userId
                    element = definedElement.name
                    response = responseString
                }
            )
        } catch (e: Exception) {
            log.warn("Failed to save MQTT message: ${e.message}")
        }
    }

    fun defineElement(topicString: String): Element? = with(topicString) {
        when {
            contains("/HBed_agr_h/") -> Element.AIR_HUMIDITY
            contains("/HBed_agr_t/") -> Element.AIR_TEMPERATURE
            contains("/HBed_agr_tds/") -> Element.FLUID_TEMPERATURE
            contains("/HBed_agr_lv/") -> Element.FLUID_LEVEL
            contains("/HBed_agr_ec/") -> Element.EC
            contains("/HBed_agr_l/") -> Element.LUX
            contains("/HBed_agr_ph/") -> Element.PH
            contains("/ClearCloudy/") -> Element.CLEAR_CLOUDY
            contains("/relay1/") -> Element.RELAY_1
            contains("/relay2/") -> Element.RELAY_2
            contains("/relay3/") -> Element.RELAY_3
            contains("/IFEC/") -> Element.IF_EC
            contains("/IFPH/") -> Element.IF_PH
            else -> {
                log.debug("Unknown topic, skip: $topicString")
                null
            }
        }
    }
}
