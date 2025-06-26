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
    ): Result<Event> {
        // Validate input
        val validationError = when {
            title.isBlank() -> "Title cannot be empty"
            description.isBlank() -> "Description cannot be empty"
            location.isBlank() -> "Location cannot be empty"
            date.isBefore(LocalDateTime.now()) -> "Event date cannot be in the past"
            else -> null
        }

        if (validationError != null) {
            return Result.failure(IllegalArgumentException(validationError))
        }

        // Check if current user is admin
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            return Result.failure(Exception("User not authenticated"))
        }
        if (currentUser.role != UserRole.ADMIN) {
            return Result.failure(Exception("Only administrators can create events"))
        }

        val event = Event(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            date = date,
            location = location,
            createdBy = currentUser.id,
        )

        return eventRepository.createEvent(event)
    }
}
