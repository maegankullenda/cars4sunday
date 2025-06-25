package com.maegankullenda.carsonsunday.ui.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maegankullenda.carsonsunday.domain.model.User
import com.maegankullenda.carsonsunday.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<WelcomeUiState>(WelcomeUiState.Loading)
    val uiState: StateFlow<WelcomeUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            if (user != null) {
                _uiState.value = WelcomeUiState.Success(user)
            } else {
                _uiState.value = WelcomeUiState.Error("No user found")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = WelcomeUiState.LoggedOut
        }
    }
}

sealed class WelcomeUiState {
    object Loading : WelcomeUiState()
    data class Success(val user: User) : WelcomeUiState()
    data class Error(val message: String) : WelcomeUiState()
    object LoggedOut : WelcomeUiState()
}
