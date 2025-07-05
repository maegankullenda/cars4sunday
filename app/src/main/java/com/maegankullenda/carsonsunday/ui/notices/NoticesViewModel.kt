package com.maegankullenda.carsonsunday.ui.notices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maegankullenda.carsonsunday.domain.model.Notice
import com.maegankullenda.carsonsunday.domain.model.User
import com.maegankullenda.carsonsunday.domain.repository.AuthRepository
import com.maegankullenda.carsonsunday.domain.usecase.GetNoticesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoticesViewModel @Inject constructor(
    private val getNoticesUseCase: GetNoticesUseCase,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<NoticesUiState>(NoticesUiState.Loading)
    val uiState: StateFlow<NoticesUiState> = _uiState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        loadCurrentUser()
        loadNotices()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            _currentUser.value = authRepository.getCurrentUser()
        }
    }

    private fun loadNotices() {
        viewModelScope.launch {
            getNoticesUseCase()
                .catch { exception ->
                    _uiState.value = NoticesUiState.Error(exception.message ?: "Failed to load notices")
                }
                .collect { notices ->
                    _uiState.value = NoticesUiState.Success(notices)
                }
        }
    }
}

sealed class NoticesUiState {
    object Loading : NoticesUiState()
    data class Success(val notices: List<Notice>) : NoticesUiState()
    data class Error(val message: String) : NoticesUiState()
}
