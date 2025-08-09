package com.maegankullenda.carsonsunday.data.source.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.maegankullenda.carsonsunday.domain.model.Event
import com.maegankullenda.carsonsunday.domain.model.EventStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class EventOfflineDataSource(context: Context) {
    
    companion object {
        private const val PREF_NAME = "offline_events"
        private const val KEY_EVENTS = "events"
        private const val KEY_LAST_SYNC = "last_sync"
    }

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREF_NAME, 
        Context.MODE_PRIVATE
    )
    private val gson = Gson()

    suspend fun saveEvents(events: List<Event>): Result<List<Event>> = withContext(Dispatchers.IO) {
        try {
            val eventsJson = gson.toJson(events)
            sharedPreferences.edit()
                .putString(KEY_EVENTS, eventsJson)
                .putLong(KEY_LAST_SYNC, System.currentTimeMillis())
                .apply()
            Result.success(events)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEvents(): Result<List<Event>> = withContext(Dispatchers.IO) {
        try {
            val eventsJson = sharedPreferences.getString(KEY_EVENTS, "[]")
            val type = object : TypeToken<List<Event>>() {}.type
            val events: List<Event> = gson.fromJson(eventsJson, type) ?: emptyList()
            Result.success(events)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUpcomingEvents(): Result<List<Event>> = withContext(Dispatchers.IO) {
        try {
            val allEvents = getEvents().getOrNull() ?: emptyList()
            val now = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            
            val upcomingEvents = allEvents.filter { event ->
                try {
                    val eventDateTime = event.date
                    eventDateTime.isAfter(now) && event.status != EventStatus.COMPLETED
                } catch (e: Exception) {
                    false
                }
            }
            
            Result.success(upcomingEvents)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEventById(eventId: String): Result<Event?> = withContext(Dispatchers.IO) {
        try {
            val events = getEvents().getOrNull() ?: emptyList()
            val event = events.find { it.id == eventId }
            Result.success(event)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearEvents(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            sharedPreferences.edit()
                .remove(KEY_EVENTS)
                .remove(KEY_LAST_SYNC)
                .apply()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLastSyncTime(): Long {
        return sharedPreferences.getLong(KEY_LAST_SYNC, 0L)
    }

    suspend fun isDataStale(maxAgeMinutes: Long = 60): Boolean {
        val lastSync = getLastSyncTime()
        val currentTime = System.currentTimeMillis()
        val maxAgeMillis = maxAgeMinutes * 60 * 1000
        return (currentTime - lastSync) > maxAgeMillis
    }
} 