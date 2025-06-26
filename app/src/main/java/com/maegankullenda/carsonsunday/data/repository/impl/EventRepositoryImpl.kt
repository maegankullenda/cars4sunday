package com.maegankullenda.carsonsunday.data.repository.impl

import com.maegankullenda.carsonsunday.data.source.local.EventLocalDataSource
import com.maegankullenda.carsonsunday.domain.model.Event
import com.maegankullenda.carsonsunday.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val eventLocalDataSource: EventLocalDataSource,
) : EventRepository {

    override suspend fun createEvent(event: Event): Result<Event> {
        return try {
            eventLocalDataSource.saveEvent(event)
            Result.success(event)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        }
    }

    override fun getEvents(): Flow<List<Event>> {
        return eventLocalDataSource.events
    }

    override suspend fun getEventById(id: String): Event? {
        return eventLocalDataSource.getEventById(id)
    }

    override suspend fun updateEvent(event: Event): Result<Event> {
        return try {
            eventLocalDataSource.updateEvent(event)
            Result.success(event)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        }
    }

    override suspend fun deleteEvent(id: String): Result<Unit> {
        return try {
            eventLocalDataSource.deleteEvent(id)
            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        }
    }

    override fun getEventsByCreator(creatorId: String): Flow<List<Event>> {
        return eventLocalDataSource.events
    }
}
