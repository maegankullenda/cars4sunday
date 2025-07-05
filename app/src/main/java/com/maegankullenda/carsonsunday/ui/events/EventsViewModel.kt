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

    init {
        loadCurrentUser()
        loadEvents()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            _currentUser.value = authRepository.getCurrentUser()
        }
    }

    private fun loadEvents() {
        viewModelScope.launch {
            _uiState.value = EventsUiState.Loading
            try {
                getEventsUseCase().collect { events ->
                    _allEvents.value = events
                    updateEventsForSelectedTab()
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

    fun getEventById(eventId: String): Event? {
        return _allEvents.value.find { it.id == eventId }
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
                // Refresh events after successful attendance
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
                // Refresh events after successful leave
                loadEvents()
            }.onFailure { exception ->
                // Handle error if needed
                println("Failed to leave event: ${exception.message}")
            }
        }
    }

    fun isUserAttending(eventId: String): Boolean {
        val currentUser = _currentUser.value
        val event = _allEvents.value.find { it.id == eventId }
        return currentUser != null && event?.attendees?.contains(currentUser.id) == true
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
