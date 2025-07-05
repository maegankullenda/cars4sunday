package com.maegankullenda.carsonsunday.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maegankullenda.carsonsunday.domain.model.Event
import com.maegankullenda.carsonsunday.domain.usecase.CreateEventUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class CreateEventViewModel @Inject constructor(
    private val createEventUseCase: CreateEventUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CreateEventUiState>(CreateEventUiState.Initial)
    val uiState: StateFlow<CreateEventUiState> = _uiState.asStateFlow()

    fun createEvent(
        title: String,
        description: String,
        date: LocalDateTime,
        location: String,
        attendeeLimit: Int? = null,
    ) {
        viewModelScope.launch {
            _uiState.value = CreateEventUiState.Loading

            createEventUseCase(title, description, date, location, attendeeLimit)
                .onSuccess { event ->
                    _uiState.value = CreateEventUiState.Success(event)
                }
                .onFailure { exception ->
                    _uiState.value = CreateEventUiState.Error(exception.message ?: "Failed to create event")
                }
        }
    }

    fun resetState() {
        _uiState.value = CreateEventUiState.Initial
    }
}

sealed class CreateEventUiState {
    object Initial : CreateEventUiState()
    object Loading : CreateEventUiState()
    data class Success(val event: Event) : CreateEventUiState()
    data class Error(val message: String) : CreateEventUiState()
}
