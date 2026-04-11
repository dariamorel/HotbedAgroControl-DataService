package org.example.service

import org.example.model.Error
import org.example.model.ErrorCode
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<Error> {
        val violations = ex.bindingResult.fieldErrors.map { fieldError ->
            "Поле ${fieldError.field}: ${fieldError.defaultMessage ?: "Ошибка валидации"}"
        }
        val error = Error(
            code = ErrorCode.VALIDATION_ERROR,
            message = violations.joinToString("\n"),
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFound(ex: UserNotFoundException): ResponseEntity<Error> {
        val error = Error(
            code = ErrorCode.USER_NOT_FOUND,
            message = ex.message ?: "Пользователь не найден по ID",
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
    }

    @ExceptionHandler(ElementDataNotFoundException::class)
    fun handleElementDataNotFound(ex: ElementDataNotFoundException): ResponseEntity<Error> {
        val error = Error(
            code = ErrorCode.ELEMENT_NOT_FOUND,
            message = ex.message ?: "Данные по элементу не найдены",
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
    }

    @ExceptionHandler(DataService.MqttConnectionException::class)
    fun handleMqttConnectionError(ex: DataService.MqttConnectionException): ResponseEntity<Error> {
        val error = Error(
            code = ErrorCode.VALIDATION_ERROR,
            message = ex.message ?: "Не удалось подключиться к MQTT broker",
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }
}

class UserNotFoundException(id: Long) : RuntimeException("Пользователь с id $id не найден")

class ElementDataNotFoundException(userId: Long, element: org.example.model.Element) :
    RuntimeException("Нет данных по элементу ${element.name} для пользователя id=$userId за выбранный период")