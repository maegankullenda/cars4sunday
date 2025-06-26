package com.maegankullenda.carsonsunday.data.source.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.maegankullenda.carsonsunday.domain.model.Event
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventLocalDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE,
    )
    private val gson = Gson()

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: Flow<List<Event>> = _events.asStateFlow()

    init {
        loadEvents()
    }

    fun saveEvent(event: Event) {
        val currentEvents = _events.value.toMutableList()
        currentEvents.add(event)
        _events.value = currentEvents
        saveEventsToStorage(currentEvents)
    }

    fun getEvents(): List<Event> {
        return _events.value
    }

    fun getEventById(id: String): Event? {
        return _events.value.find { it.id == id }
    }

    fun updateEvent(event: Event) {
        val currentEvents = _events.value.toMutableList()
        val index = currentEvents.indexOfFirst { it.id == event.id }
        if (index != -1) {
            currentEvents[index] = event
            _events.value = currentEvents
            saveEventsToStorage(currentEvents)
        }
    }

    fun deleteEvent(id: String) {
        val currentEvents = _events.value.toMutableList()
        currentEvents.removeAll { it.id == id }
        _events.value = currentEvents
        saveEventsToStorage(currentEvents)
    }

    fun getEventsByCreator(creatorId: String): List<Event> {
        return _events.value.filter { it.createdBy == creatorId }
    }

    private fun loadEvents() {
        val eventsJson = prefs.getString(KEY_EVENTS, "[]")
        val eventsList: List<EventDto> = gson.fromJson(eventsJson, object : TypeToken<List<EventDto>>() {}.type)
        _events.value = eventsList.map { it.toEvent() }
    }

    private fun saveEventsToStorage(events: List<Event>) {
        val eventsDto = events.map { EventDto.fromEvent(it) }
        val eventsJson = gson.toJson(eventsDto)
        prefs.edit().putString(KEY_EVENTS, eventsJson).apply()
    }

    companion object {
        private const val PREFS_NAME = "events_prefs"
        private const val KEY_EVENTS = "events"
        private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    }

    private data class EventDto(
        val id: String,
        val title: String,
        val description: String,
        val date: String,
        val location: String,
        val createdBy: String,
        val createdAt: String,
        val isActive: Boolean,
    ) {
        fun toEvent(): Event {
            return Event(
                id = id,
                title = title,
                description = description,
                date = LocalDateTime.parse(date, dateFormatter),
                location = location,
                createdBy = createdBy,
                createdAt = LocalDateTime.parse(createdAt, dateFormatter),
                isActive = isActive,
            )
        }

        companion object {
            fun fromEvent(event: Event): EventDto {
                return EventDto(
                    id = event.id,
                    title = event.title,
                    description = event.description,
                    date = event.date.format(dateFormatter),
                    location = event.location,
                    createdBy = event.createdBy,
                    createdAt = event.createdAt.format(dateFormatter),
                    isActive = event.isActive,
                )
            }
        }
    }
}
