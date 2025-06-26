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
        if (title.isBlank()) {
            return Result.failure(IllegalArgumentException("Title cannot be empty"))
        }
        if (description.isBlank()) {
            return Result.failure(IllegalArgumentException("Description cannot be empty"))
        }
        if (location.isBlank()) {
            return Result.failure(IllegalArgumentException("Location cannot be empty"))
        }
        if (date.isBefore(LocalDateTime.now())) {
            return Result.failure(IllegalArgumentException("Event date cannot be in the past"))
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
