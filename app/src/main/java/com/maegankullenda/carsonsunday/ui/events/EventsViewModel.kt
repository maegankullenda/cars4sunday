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
import com.maegankullenda.carsonsunday.util.CalendarAccount
import com.maegankullenda.carsonsunday.util.CalendarManager
import com.maegankullenda.carsonsunday.util.NotificationTestHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val getEventsUseCase: GetEventsUseCase,
    private val attendEventUseCase: AttendEventUseCase,
    private val leaveEventUseCase: LeaveEventUseCase,
    private val authRepository: AuthRepository,
    private val eventRepository: EventRepository,
    private val calendarManager: CalendarManager,
    private val notificationTestHelper: NotificationTestHelper,
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

    // Permission callback
    private var permissionCallback: (() -> Unit)? = null

    // Calendar integration observable state
    private val _hasCalendarPermission = MutableStateFlow(false)
    val hasCalendarPermissionState: StateFlow<Boolean> = _hasCalendarPermission.asStateFlow()

    private val _hasCalendarAccount = MutableStateFlow(false)
    val hasCalendarAccountState: StateFlow<Boolean> = _hasCalendarAccount.asStateFlow()

    init {
        loadCurrentUser()
        loadEvents()
        refreshCalendarIntegrationState()
        // Start periodic check for completed events
        startPeriodicEventStatusCheck()
    }

    fun refreshCalendarIntegrationState() {
        _hasCalendarPermission.value = calendarManager.hasCalendarPermission()
        _hasCalendarAccount.value = if (_hasCalendarPermission.value) {
            calendarManager.hasGoogleAccountSetUp()
        } else {
            false
        }
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
                    // Check and update event statuses before filtering
                    checkAndUpdateEventStatuses()
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

    /**
     * Checks if an event should be marked as completed (date has passed by one day)
     */
    private fun shouldEventBeCompleted(event: Event): Boolean {
        val now = LocalDateTime.now()
        val eventDate = event.date
        val oneDayAfterEvent = eventDate.plusDays(1)

        return event.status == EventStatus.UPCOMING && now.isAfter(oneDayAfterEvent)
    }

    /**
     * Checks all events and updates their status to COMPLETED if they should be
     */
    private fun checkAndUpdateEventStatuses() {
        viewModelScope.launch {
            val eventsToUpdate = _allEvents.value.filter { shouldEventBeCompleted(it) }

            eventsToUpdate.forEach { event ->
                val updatedEvent = event.copy(status = EventStatus.COMPLETED)
                eventRepository.updateEvent(updatedEvent).onSuccess {
                    // Event updated successfully
                }.onFailure { exception ->
                    println("Failed to update event status to completed: ${exception.message}")
                }
            }

            // If any events were updated, reload events to get the updated list
            if (eventsToUpdate.isNotEmpty()) {
                loadEvents()
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

    fun selectTab(status: EventStatus) {
        _selectedTab.value = status
        updateEventsForSelectedTab()
    }

    private fun updateAttendanceStatus(events: List<Event>) {
        viewModelScope.launch {
            val currentUser = _currentUser.value
            if (currentUser != null) {
                val newAttendanceStatus = _attendanceStatus.value.toMutableMap()
                events.forEach { event ->
                    newAttendanceStatus[event.id] = event.attendees.contains(currentUser.id)
                }
                _attendanceStatus.value = newAttendanceStatus
            }
        }
    }

    private fun updateAttendanceStatusForAllEvents() {
        updateAttendanceStatus(_allEvents.value)
    }

    fun refreshEvents() {
        loadEvents()
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
            result.onSuccess { updatedEvent ->
                // Test notification for event update (remove after Cloud Functions are deployed)
                testEventUpdateNotification(updatedEvent, "has been updated")
                
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
                
                // Add event to calendar
                addEventToCalendar(updatedEvent)
                
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
                
                // Remove event from calendar
                removeEventFromCalendar(updatedEvent)
                
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

    // ----- Calendar helpers -----

    private fun addEventToCalendar(event: Event) {
        viewModelScope.launch {
            if (!calendarManager.hasCalendarPermission()) {
                println("Calendar permission not granted")
                refreshCalendarIntegrationState()
                return@launch
            }

            if (!calendarManager.hasGoogleAccountSetUp()) {
                println("Google account not set up for calendar")
                refreshCalendarIntegrationState()
                return@launch
            }

            if (calendarManager.isEventInCalendar(event)) {
                println("Event already in calendar")
                return@launch
            }

            calendarManager.addEventToCalendar(event).onSuccess { calendarEventId ->
                println("Successfully added event to calendar with ID: $calendarEventId")
            }.onFailure { exception ->
                println("Failed to add event to calendar: ${exception.message}")
            }
            refreshCalendarIntegrationState()
        }
    }

    private fun removeEventFromCalendar(event: Event) {
        viewModelScope.launch {
            if (!calendarManager.hasCalendarPermission()) {
                println("Calendar permission not granted")
                refreshCalendarIntegrationState()
                return@launch
            }

            calendarManager.removeEventFromCalendar(event).onSuccess {
                println("Successfully removed event from calendar")
            }.onFailure { exception ->
                println("Failed to remove event from calendar: ${exception.message}")
            }
            refreshCalendarIntegrationState()
        }
    }

    fun hasCalendarPermission(): Boolean {
        return calendarManager.hasCalendarPermission()
    }

    fun isEventInCalendar(event: Event): Boolean {
        return calendarManager.isEventInCalendar(event)
    }

    fun hasGoogleAccountSetUp(): Boolean {
        return calendarManager.hasGoogleAccountSetUp()
    }

    fun getAvailableCalendars(): List<CalendarAccount> {
        return calendarManager.getAvailableCalendars()
    }

    fun setPermissionCallback(callback: () -> Unit) {
        permissionCallback = callback
    }

    fun requestCalendarPermissions() {
        permissionCallback?.invoke()
    }

    // Re-introduced no-op periodic check to satisfy references
    private fun startPeriodicEventStatusCheck() {
        // No-op
    }

    // Expose repository methods used by other screens
    suspend fun getEventById(eventId: String): Event? {
        return eventRepository.getEventById(eventId)
    }

    suspend fun getRespondentsForEvent(eventId: String): List<User> {
        val event = _allEvents.value.find { it.id == eventId } ?: eventRepository.getEventById(eventId)
        if (event == null || event.attendees.isEmpty()) return emptyList()
        return event.attendees.mapNotNull { userId ->
            authRepository.getUserById(userId)
        }
    }

    // Test notification methods (remove these after Cloud Functions are deployed)
    fun testNewEventNotification(event: Event) {
        viewModelScope.launch {
            notificationTestHelper.testNewEventNotification(event)
        }
    }

    fun testEventUpdateNotification(event: Event, changeDescription: String = "has been updated") {
        viewModelScope.launch {
            notificationTestHelper.testEventUpdateNotification(event, changeDescription)
        }
    }

    fun testFCMMessage() {
        viewModelScope.launch {
            notificationTestHelper.sendTestFCMMessage()
        }
    }
}

sealed class EventsUiState {
    object Loading : EventsUiState()
    data class Success(val events: List<Event>) : EventsUiState()
    data class Error(val message: String) : EventsUiState()
}
