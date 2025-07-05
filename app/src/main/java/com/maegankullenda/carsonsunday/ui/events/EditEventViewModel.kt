package com.maegankullenda.carsonsunday.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maegankullenda.carsonsunday.domain.model.Event
import com.maegankullenda.carsonsunday.domain.model.UserRole
import com.maegankullenda.carsonsunday.domain.repository.AuthRepository
import com.maegankullenda.carsonsunday.domain.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class EditEventViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<EditEventUiState>(EditEventUiState.Initial)
    val uiState: StateFlow<EditEventUiState> = _uiState.asStateFlow()

    private val _event = MutableStateFlow<Event?>(null)
    val event: StateFlow<Event?> = _event.asStateFlow()

    fun loadEvent(eventId: String) {
        viewModelScope.launch {
            _uiState.value = EditEventUiState.Loading
            try {
                val event = eventRepository.getEventById(eventId)
                if (event != null) {
                    _event.value = event
                    _uiState.value = EditEventUiState.Initial
                } else {
                    _uiState.value = EditEventUiState.Error("Event not found")
                }
            } catch (e: Exception) {
                _uiState.value = EditEventUiState.Error("Failed to load event: ${e.message}")
            }
        }
    }

    fun updateEvent(
        title: String,
        description: String,
        date: LocalDateTime,
        location: String,
    ) {
        viewModelScope.launch {
            _uiState.value = EditEventUiState.Loading

            // Validate input
            if (title.isBlank()) {
                _uiState.value = EditEventUiState.Error("Title cannot be empty")
                return@launch
            }
            if (description.isBlank()) {
                _uiState.value = EditEventUiState.Error("Description cannot be empty")
                return@launch
            }
            if (location.isBlank()) {
                _uiState.value = EditEventUiState.Error("Location cannot be empty")
                return@launch
            }
            if (date.isBefore(LocalDateTime.now())) {
                _uiState.value = EditEventUiState.Error("Event date cannot be in the past")
                return@launch
            }

            // Check if current user is admin
            val currentUser = authRepository.getCurrentUser()
            if (currentUser == null) {
                _uiState.value = EditEventUiState.Error("User not authenticated")
                return@launch
            }
            if (currentUser.role != UserRole.ADMIN) {
                _uiState.value = EditEventUiState.Error("Only administrators can edit events")
                return@launch
            }

            try {
                val currentEvent = _event.value
                if (currentEvent != null) {
                    val updatedEvent = currentEvent.copy(
                        title = title,
                        description = description,
                        date = date,
                        location = location,
                    )

                    val result = eventRepository.updateEvent(updatedEvent)
                    result.onSuccess {
                        _uiState.value = EditEventUiState.Success(it)
                    }.onFailure { exception ->
                        _uiState.value = EditEventUiState.Error("Failed to update event: ${exception.message}")
                    }
                } else {
                    _uiState.value = EditEventUiState.Error("Event not found")
                }
            } catch (e: IllegalStateException) {
                _uiState.value = EditEventUiState.Error("Failed to update event: ${e.message}")
            } catch (e: IllegalArgumentException) {
                _uiState.value = EditEventUiState.Error("Invalid event data: ${e.message}")
            }
        }
    }

    fun resetState() {
        _uiState.value = EditEventUiState.Initial
    }
}

sealed class EditEventUiState {
    object Initial : EditEventUiState()
    object Loading : EditEventUiState()
    data class Success(val event: Event) : EditEventUiState()
    data class Error(val message: String) : EditEventUiState()
}
