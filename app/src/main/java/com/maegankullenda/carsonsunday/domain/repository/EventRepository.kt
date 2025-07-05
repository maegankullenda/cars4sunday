package com.maegankullenda.carsonsunday.domain.repository

import com.maegankullenda.carsonsunday.domain.model.Event
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    suspend fun createEvent(event: Event): Result<Event>
    fun getEvents(): Flow<List<Event>>
    suspend fun getEventById(id: String): Event?
    suspend fun updateEvent(event: Event): Result<Event>
    suspend fun deleteEvent(id: String): Result<Unit>
    fun getEventsByCreator(creatorId: String): Flow<List<Event>>

    // Attendance methods
    suspend fun attendEvent(eventId: String, userId: String): Result<Event>
    suspend fun leaveEvent(eventId: String, userId: String): Result<Event>
    suspend fun isUserAttending(eventId: String, userId: String): Boolean
}
