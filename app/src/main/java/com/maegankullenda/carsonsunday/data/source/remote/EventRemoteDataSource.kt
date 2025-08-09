package com.maegankullenda.carsonsunday.data.source.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.maegankullenda.carsonsunday.domain.model.Event
import com.maegankullenda.carsonsunday.domain.model.EventStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private val eventsCollection = firestore.collection(COLLECTION_EVENTS)

    suspend fun saveEvent(event: Event): Result<Event> {
        return try {
            val eventData = mapOf(
                "id" to event.id,
                "title" to event.title,
                "description" to event.description,
                "date" to event.date.format(dateFormatter),
                "location" to event.location,
                "createdBy" to event.createdBy,
                "createdAt" to event.createdAt.format(dateFormatter),
                "isActive" to event.isActive,
                "status" to event.status.name,
                "attendees" to event.attendees,
                "attendeeLimit" to event.attendeeLimit,
            )

            eventsCollection.document(event.id).set(eventData).await()
            Result.success(event)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEventById(eventId: String): Event? {
        return try {
            val document = eventsCollection.document(eventId).get().await()
            if (document.exists()) {
                document.toEvent()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAllEvents(): List<Event> {
        return try {
            val query = eventsCollection.get().await()
            query.documents.mapNotNull { it.toEvent() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getEventsByCreator(creatorId: String): List<Event> {
        return try {
            val query = eventsCollection.whereEqualTo("createdBy", creatorId).get().await()
            query.documents.mapNotNull { it.toEvent() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getEventsByStatus(status: EventStatus): List<Event> {
        return try {
            val query = eventsCollection.whereEqualTo("status", status.name).get().await()
            query.documents.mapNotNull { it.toEvent() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateEvent(event: Event): Result<Event> {
        return try {
            val eventData = mapOf(
                "id" to event.id,
                "title" to event.title,
                "description" to event.description,
                "date" to event.date.format(dateFormatter),
                "location" to event.location,
                "createdBy" to event.createdBy,
                "createdAt" to event.createdAt.format(dateFormatter),
                "isActive" to event.isActive,
                "status" to event.status.name,
                "attendees" to event.attendees,
                "attendeeLimit" to event.attendeeLimit,
            )

            eventsCollection.document(event.id).update(eventData).await()
            Result.success(event)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteEvent(eventId: String): Result<Unit> {
        return try {
            eventsCollection.document(eventId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun attendEvent(eventId: String, userId: String): Result<Event> {
        return try {
            val event = getEventById(eventId)
            if (event == null) {
                return Result.failure(Exception("Event not found"))
            }

            if (event.attendees.contains(userId)) {
                return Result.failure(Exception("Already attending this event"))
            }

            val updatedEvent = event.copy(attendees = event.attendees + userId)
            updateEvent(updatedEvent).map { updatedEvent }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun leaveEvent(eventId: String, userId: String): Result<Event> {
        return try {
            val event = getEventById(eventId)
            if (event == null) {
                return Result.failure(Exception("Event not found"))
            }

            if (!event.attendees.contains(userId)) {
                return Result.failure(Exception("Not attending this event"))
            }

            val updatedEvent = event.copy(attendees = event.attendees - userId)
            updateEvent(updatedEvent).map { updatedEvent }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isUserAttending(eventId: String, userId: String): Boolean {
        return try {
            val event = getEventById(eventId)
            event?.attendees?.contains(userId) == true
        } catch (e: Exception) {
            false
        }
    }

    fun observeEvents(): Flow<List<Event>> = callbackFlow {
        val listener = eventsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val events = snapshot?.documents?.mapNotNull { it.toEvent() } ?: emptyList()
            trySend(events)
        }

        awaitClose { listener.remove() }
    }

    fun observeEventsByStatus(status: EventStatus): Flow<List<Event>> = callbackFlow {
        val listener = eventsCollection
            .whereEqualTo("status", status.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val events = snapshot?.documents?.mapNotNull { it.toEvent() } ?: emptyList()
                trySend(events)
            }

        awaitClose { listener.remove() }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toEvent(): Event? {
        return try {
            val id = getString("id") ?: return null
            val title = getString("title") ?: return null
            val description = getString("description") ?: return null
            val dateString = getString("date") ?: return null
            val location = getString("location") ?: return null
            val createdBy = getString("createdBy") ?: return null
            val createdAtString = getString("createdAt") ?: return null
            val isActive = getBoolean("isActive") ?: return null
            val statusString = getString("status") ?: return null
            val attendees = get("attendees") as? List<String> ?: emptyList()
            val attendeeLimit = getLong("attendeeLimit")?.toInt()

            Event(
                id = id,
                title = title,
                description = description,
                date = LocalDateTime.parse(dateString, dateFormatter),
                location = location,
                createdBy = createdBy,
                createdAt = LocalDateTime.parse(createdAtString, dateFormatter),
                isActive = isActive,
                status = EventStatus.valueOf(statusString),
                attendees = attendees,
                attendeeLimit = attendeeLimit,
            )
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        private const val COLLECTION_EVENTS = "events"
        private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    }
}
