package com.maegankullenda.carsonsunday.domain.usecase

import com.maegankullenda.carsonsunday.domain.model.Event
import com.maegankullenda.carsonsunday.domain.model.EventStatus
import com.maegankullenda.carsonsunday.domain.repository.AuthRepository
import com.maegankullenda.carsonsunday.domain.repository.EventRepository
import javax.inject.Inject

class AttendEventUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(eventId: String): Result<Event> {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            return Result.failure(Exception("User not authenticated"))
        }

        val event = eventRepository.getEventById(eventId)
        if (event == null) {
            return Result.failure(Exception("Event not found"))
        }

        if (event.status != EventStatus.UPCOMING) {
            return Result.failure(Exception("Cannot attend a non-upcoming event"))
        }

        if (event.isAtCapacity) {
            return Result.failure(Exception("Event is at capacity"))
        }

        if (event.attendees.contains(currentUser.id)) {
            return Result.failure(Exception("Already attending this event"))
        }

        return eventRepository.attendEvent(eventId, currentUser.id)
    }
}
