package com.maegankullenda.carsonsunday.domain.usecase

import com.maegankullenda.carsonsunday.domain.model.Event
import com.maegankullenda.carsonsunday.domain.model.EventStatus
import com.maegankullenda.carsonsunday.domain.repository.AuthRepository
import com.maegankullenda.carsonsunday.domain.repository.EventRepository
import javax.inject.Inject

class LeaveEventUseCase @Inject constructor(
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
            return Result.failure(Exception("Cannot leave a non-upcoming event"))
        }

        if (!event.attendees.contains(currentUser.id)) {
            return Result.failure(Exception("Not attending this event"))
        }

        return eventRepository.leaveEvent(eventId, currentUser.id)
    }
}
