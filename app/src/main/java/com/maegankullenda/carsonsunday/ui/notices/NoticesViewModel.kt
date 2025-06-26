package com.maegankullenda.carsonsunday.ui.notices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maegankullenda.carsonsunday.domain.model.Notice
import com.maegankullenda.carsonsunday.domain.model.User
import com.maegankullenda.carsonsunday.domain.model.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoticesViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow<NoticesUiState>(NoticesUiState.Loading)
    val uiState: StateFlow<NoticesUiState> = _uiState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(
        User(
            "userId",
            "username",
            "password",
            "Name",
            "Surname",
            "1234567890",
            UserRole.ADMIN,
        ),
    )
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        loadNotices()
    }

    private fun loadNotices() {
        viewModelScope.launch {
            // Load notices from repository (placeholder implementation)
            _uiState.value = NoticesUiState.Success(listOf())
        }
    }
}

sealed class NoticesUiState {
    object Loading : NoticesUiState()
    data class Success(val notices: List<Notice>) : NoticesUiState()
    data class Error(val message: String) : NoticesUiState()
}
