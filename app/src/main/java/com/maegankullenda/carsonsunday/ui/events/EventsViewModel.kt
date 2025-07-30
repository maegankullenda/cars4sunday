package com.maegankullenda.carsonsunday.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maegankullenda.carsonsunday.domain.model.Event
import com.maegankullenda.carsonsunday.domain.model.EventStatus
import com.maegankullenda.carsonsunday.domain.model.User
import com.maegankullenda.carsonsunday.domain.model.UserRole
import com.maegankullenda.carsonsunday.domain.repository.AuthRepository
import com.maegankullenda.carsonsunday.domain.repository.EventRepository
import com.maegankullenda.carsonsunday.domain.usecase.AttendEventUseCase
import com.maegankullenda.carsonsunday.domain.usecase.GetEventsUseCase
import com.maegankullenda.carsonsunday.domain.usecase.LeaveEventUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val getEventsUseCase: GetEventsUseCase,
    private val attendEventUseCase: AttendEventUseCase,
    private val leaveEventUseCase: LeaveEventUseCase,
    private val authRepository: AuthRepository,
    private val eventRepository: EventRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<EventsUiState>(EventsUiState.Loading)
    val uiState: StateFlow<EventsUiState> = _uiState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _selectedTab = MutableStateFlow(EventStatus.UPCOMING)
    val selectedTab: StateFlow<EventStatus> = _selectedTab.asStateFlow()

    private val _allEvents = MutableStateFlow<List<Event>>(emptyList())
    private val _attendanceStatus = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val attendanceStatus: StateFlow<Map<String, Boolean>> = _attendanceStatus.asStateFlow()

    init {
        loadCurrentUser()
        loadEvents()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            _currentUser.value = authRepository.getCurrentUser()
            // After loading the current user, update attendance status for all events
            updateAttendanceStatusForAllEvents()
        }
    }

    private fun loadEvents() {
        viewModelScope.launch {
            _uiState.value = EventsUiState.Loading
            try {
                getEventsUseCase().collect { events ->
                    _allEvents.value = events
                    updateEventsForSelectedTab()
                    // Update attendance status for all events after loading
                    updateAttendanceStatusForAllEvents()
                }
            } catch (e: IOException) {
                _uiState.value = EventsUiState.Error("Failed to load events: ${e.message}")
            } catch (e: IllegalArgumentException) {
                _uiState.value = EventsUiState.Error("Invalid data: ${e.message}")
            }
        }
    }

    private fun updateEventsForSelectedTab() {
        val filteredEvents = when (_selectedTab.value) {
            EventStatus.UPCOMING ->
                _allEvents.value
                    .filter { it.status == EventStatus.UPCOMING }
                    .sortedBy { it.date }
            EventStatus.COMPLETED ->
                _allEvents.value
                    .filter { it.status == EventStatus.COMPLETED }
                    .sortedByDescending { it.date }
            EventStatus.CANCELLED ->
                _allEvents.value
                    .filter { it.status == EventStatus.CANCELLED }
                    .sortedByDescending { it.date }
        }
        _uiState.value = EventsUiState.Success(filteredEvents)
        updateAttendanceStatus(filteredEvents)
    }

    private fun updateAttendanceStatus(events: List<Event>) {
        viewModelScope.launch {
            val currentUser = _currentUser.value
            if (currentUser != null) {
                val newAttendanceStatus = _attendanceStatus.value.toMutableMap()
                events.forEach { event ->
                    // Only update if we don't already have a cached value for this event
                    // This preserves manual updates from attendEvent/leaveEvent
                    if (!newAttendanceStatus.containsKey(event.id)) {
                        try {
                            newAttendanceStatus[event.id] = eventRepository.isUserAttending(event.id, currentUser.id)
                        } catch (e: Exception) {
                            // If we can't get the attendance status, default to false
                            newAttendanceStatus[event.id] = false
                        }
                    }
                }
                _attendanceStatus.value = newAttendanceStatus
            }
        }
    }

    private fun updateAttendanceStatusForAllEvents() {
        viewModelScope.launch {
            val currentUser = _currentUser.value
            if (currentUser != null) {
                val newAttendanceStatus = _attendanceStatus.value.toMutableMap()
                _allEvents.value.forEach { event ->
                    // Only update if we don't already have a cached value for this event
                    // This preserves manual updates from attendEvent/leaveEvent
                    if (!newAttendanceStatus.containsKey(event.id)) {
                        try {
                            newAttendanceStatus[event.id] = eventRepository.isUserAttending(event.id, currentUser.id)
                        } catch (e: Exception) {
                            newAttendanceStatus[event.id] = false
                        }
                    }
                }
                _attendanceStatus.value = newAttendanceStatus
            }
        }
    }

    fun selectTab(status: EventStatus) {
        _selectedTab.value = status
        updateEventsForSelectedTab()
    }

    fun isAdmin(): Boolean {
        return currentUser.value?.role == UserRole.ADMIN
    }

    fun refreshEvents() {
        loadEvents()
    }

    suspend fun getEventById(eventId: String): Event? {
        return eventRepository.getEventById(eventId)
    }

    fun cancelEvent(eventId: String) {
        viewModelScope.launch {
            val event = _allEvents.value.find { it.id == eventId }
            event?.let { currentEvent ->
                val cancelledEvent = currentEvent.copy(status = EventStatus.CANCELLED)
                // Update the event in the repository
                val result = eventRepository.updateEvent(cancelledEvent)
                result.onSuccess {
                    // Refresh events after successful update
                    loadEvents()
                }.onFailure { exception ->
                    // Handle error if needed
                    println("Failed to cancel event: ${exception.message}")
                }
            }
        }
    }

    fun updateEvent(event: Event) {
        viewModelScope.launch {
            val result = eventRepository.updateEvent(event)
            result.onSuccess {
                // Refresh events after successful update
                loadEvents()
            }.onFailure { exception ->
                // Handle error if needed
                println("Failed to update event: ${exception.message}")
            }
        }
    }

    fun attendEvent(eventId: String) {
        viewModelScope.launch {
            attendEventUseCase(eventId).onSuccess { updatedEvent ->
                // Update attendance status for this specific event immediately
                val currentUser = _currentUser.value
                if (currentUser != null) {
                    val newStatus = _attendanceStatus.value.toMutableMap()
                    newStatus[eventId] = true
                    _attendanceStatus.value = newStatus
                }
                // Refresh events to get the updated event data
                loadEvents()
            }.onFailure { exception ->
                // Handle error if needed
                println("Failed to attend event: ${exception.message}")
            }
        }
    }

    fun leaveEvent(eventId: String) {
        viewModelScope.launch {
            leaveEventUseCase(eventId).onSuccess { updatedEvent ->
                // Update attendance status for this specific event immediately
                val currentUser = _currentUser.value
                if (currentUser != null) {
                    val newStatus = _attendanceStatus.value.toMutableMap()
                    newStatus[eventId] = false
                    _attendanceStatus.value = newStatus
                }
                // Refresh events to get the updated event data
                loadEvents()
            }.onFailure { exception ->
                // Handle error if needed
                println("Failed to leave event: ${exception.message}")
            }
        }
    }

    fun isUserAttending(eventId: String): Boolean {
        return _attendanceStatus.value[eventId] ?: false
    }

    // Test helper method
    fun setAttendanceStatus(eventId: String, isAttending: Boolean) {
        val newStatus = _attendanceStatus.value.toMutableMap()
        newStatus[eventId] = isAttending
        _attendanceStatus.value = newStatus
    }

    suspend fun getRespondentsForEvent(eventId: String): List<User> {
        val event = _allEvents.value.find { it.id == eventId }
        if (event == null || event.attendees.isEmpty()) {
            return emptyList()
        }

        // Get real user data for each attendee
        return event.attendees.mapNotNull { userId ->
            authRepository.getUserById(userId)
        }
    }
}

sealed class EventsUiState {
    object Loading : EventsUiState()
    data class Success(val events: List<Event>) : EventsUiState()
    data class Error(val message: String) : EventsUiState()
}
