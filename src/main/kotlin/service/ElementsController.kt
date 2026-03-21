package org.example.service

import org.example.api.HistoryApi
import org.example.model.Element
import org.example.model.ElementListResponse
import org.example.model.ElementResponse
import org.example.model.Period
import org.example.repository.ElementsRepository
import org.example.repository.UsersRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

@RestController
open class ElementsController(
    private val elementsRepository: ElementsRepository,
    private val usersRepository: UsersRepository
) : HistoryApi {

    override fun getDataHistory(
        id: Long,
        element: Element,
        time: OffsetDateTime,
        period: Period
    ): ResponseEntity<ElementListResponse> {
        if (!usersRepository.existsById(id)) {
            throw UserNotFoundException(id)
        }
        val (startTime, endTime) = when (period) {
            Period.HOUR -> {
                val start = time.truncatedTo(ChronoUnit.HOURS)
                start to start.plusHours(1)
            }
            Period.DAY -> {
                val start = time.truncatedTo(ChronoUnit.DAYS)
                start to start.plusDays(1)
            }
            Period.MONTH -> {
                val start = time.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS)
                start to start.plusMonths(1)
            }
            Period.YEAR -> {
                val start = time.withDayOfYear(1).truncatedTo(ChronoUnit.DAYS)
                start to start.plusYears(1)
            }
        }
        val elements = elementsRepository.findAllElements(id, element.name, startTime, endTime)
        if (elements.isEmpty()) {
            throw ElementDataNotFoundException(id, element)
        }
        val response = ElementListResponse(
            content = elements.mapNotNull { entity ->
                val definedElement = defineElement(entity.element)
                definedElement?.let {
                    ElementResponse(
                        userId = entity.userId,
                        element = it,
                        time = entity.time?.withOffsetSameInstant(ZoneOffset.ofHours(3)) ?: OffsetDateTime.now(),
                        response = entity.response
                    )
                }
            },
            totalElements = elements.size.toLong()
        )
        return ResponseEntity.ok(response)
    }

    private fun defineElement(elementName: String): Element? =
        Element.entries.find { it.name == elementName }
}