package com.maegankullenda.carsonsunday.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maegankullenda.carsonsunday.domain.model.Event
import com.maegankullenda.carsonsunday.domain.model.User
import com.maegankullenda.carsonsunday.domain.model.UserRole
import com.maegankullenda.carsonsunday.domain.repository.AuthRepository
import com.maegankullenda.carsonsunday.domain.usecase.GetEventsUseCase
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
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<EventsUiState>(EventsUiState.Loading)
    val uiState: StateFlow<EventsUiState> = _uiState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

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
                    _uiState.value = EventsUiState.Success(events)
                }
            } catch (e: IOException) {
                _uiState.value = EventsUiState.Error("Failed to load events: ${e.message}")
            } catch (e: IllegalArgumentException) {
                _uiState.value = EventsUiState.Error("Invalid data: ${e.message}")
            }
        }
    }

    fun isAdmin(): Boolean {
        return currentUser.value?.role == UserRole.ADMIN
    }

    fun refreshEvents() {
        loadEvents()
    }
}

sealed class EventsUiState {
    object Loading : EventsUiState()
    data class Success(val events: List<Event>) : EventsUiState()
    data class Error(val message: String) : EventsUiState()
}
