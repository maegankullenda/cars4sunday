package com.maegankullenda.carsonsunday.domain.usecase

import com.maegankullenda.carsonsunday.domain.model.Event
import com.maegankullenda.carsonsunday.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetEventsUseCase @Inject constructor(
    private val eventRepository: EventRepository,
) {
    operator fun invoke(): Flow<List<Event>> {
        return eventRepository.getEvents()
    }
}
