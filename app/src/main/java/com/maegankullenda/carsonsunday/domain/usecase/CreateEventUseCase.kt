@file:Suppress("ReturnCount")

package com.maegankullenda.carsonsunday.domain.usecase

import com.maegankullenda.carsonsunday.domain.model.Event
import com.maegankullenda.carsonsunday.domain.model.UserRole
import com.maegankullenda.carsonsunday.domain.repository.AuthRepository
import com.maegankullenda.carsonsunday.domain.repository.EventRepository
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

class CreateEventUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        title: String,
        description: String,
        date: LocalDateTime,
        location: String,
        attendeeLimit: Int? = null,
    ): Result<Event> {
        // Validate input and user permissions
        val validationResult = validateInputAndPermissions(title, description, date, location, attendeeLimit)
        if (validationResult.isFailure) {
            return Result.failure(validationResult.exceptionOrNull() ?: Exception("Validation failed"))
        }

        val currentUser = validationResult.getOrNull()!!
        val event = Event(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            date = date,
            location = location,
            attendeeLimit = attendeeLimit,
            attendees = emptyList(), // New events start with no attendees
            createdBy = currentUser.id,
        )

        return eventRepository.createEvent(event)
    }

    private suspend fun validateInputAndPermissions(
        title: String,
        description: String,
        date: LocalDateTime,
        location: String,
        attendeeLimit: Int?,
    ): Result<com.maegankullenda.carsonsunday.domain.model.User> {
        val validationError = when {
            title.isBlank() -> "Title cannot be empty"
            description.isBlank() -> "Description cannot be empty"
            location.isBlank() -> "Location cannot be empty"
            date.isBefore(LocalDateTime.now()) -> "Event date cannot be in the past"
            attendeeLimit != null && attendeeLimit <= 0 -> "Attendee limit must be greater than 0"
            else -> null
        }

        if (validationError != null) {
            return Result.failure(IllegalArgumentException(validationError))
        }

        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            return Result.failure(Exception("User not authenticated"))
        }
        if (currentUser.role != UserRole.ADMIN) {
            return Result.failure(Exception("Only administrators can create events"))
        }

        return Result.success(currentUser)
    }
}
