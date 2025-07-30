package com.maegankullenda.carsonsunday.data.repository.impl

import com.maegankullenda.carsonsunday.data.source.DataSourceManager
import com.maegankullenda.carsonsunday.domain.model.Event
import com.maegankullenda.carsonsunday.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val dataSourceManager: DataSourceManager,
) : EventRepository {

    override suspend fun createEvent(event: Event): Result<Event> {
        return try {
            val eventDataSource = dataSourceManager.getEventDataSource()
            eventDataSource.saveEvent(event)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getEvents(): Flow<List<Event>> {
        val eventDataSource = dataSourceManager.getEventDataSource()
        return eventDataSource.observeEvents()
    }

    override suspend fun getEventById(id: String): Event? {
        val eventDataSource = dataSourceManager.getEventDataSource()
        return eventDataSource.getEventById(id)
    }

    override suspend fun updateEvent(event: Event): Result<Event> {
        return try {
            val eventDataSource = dataSourceManager.getEventDataSource()
            eventDataSource.updateEvent(event)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteEvent(id: String): Result<Unit> {
        return try {
            val eventDataSource = dataSourceManager.getEventDataSource()
            eventDataSource.deleteEvent(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getEventsByCreator(creatorId: String): Flow<List<Event>> {
        val eventDataSource = dataSourceManager.getEventDataSource()
        return eventDataSource.observeEvents()
    }

    override suspend fun attendEvent(eventId: String, userId: String): Result<Event> {
        return try {
            val eventDataSource = dataSourceManager.getEventDataSource()
            eventDataSource.attendEvent(eventId, userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun leaveEvent(eventId: String, userId: String): Result<Event> {
        return try {
            val eventDataSource = dataSourceManager.getEventDataSource()
            eventDataSource.leaveEvent(eventId, userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isUserAttending(eventId: String, userId: String): Boolean {
        val eventDataSource = dataSourceManager.getEventDataSource()
        return eventDataSource.isUserAttending(eventId, userId)
    }
}
