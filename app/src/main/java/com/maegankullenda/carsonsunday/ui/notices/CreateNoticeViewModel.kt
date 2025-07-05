package com.maegankullenda.carsonsunday.ui.notices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maegankullenda.carsonsunday.domain.model.Notice
import com.maegankullenda.carsonsunday.domain.model.NoticePriority
import com.maegankullenda.carsonsunday.domain.usecase.CreateNoticeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateNoticeViewModel @Inject constructor(
    private val createNoticeUseCase: CreateNoticeUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<CreateNoticeUiState>(CreateNoticeUiState.Initial)
    val uiState: StateFlow<CreateNoticeUiState> = _uiState.asStateFlow()

    fun createNotice(title: String, content: String, priority: NoticePriority) {
        viewModelScope.launch {
            _uiState.value = CreateNoticeUiState.Loading
            createNoticeUseCase(title, content, priority)
                .onSuccess { notice ->
                    _uiState.value = CreateNoticeUiState.Success(notice)
                }
                .onFailure { exception ->
                    _uiState.value = CreateNoticeUiState.Error(exception.message ?: "Failed to create notice")
                }
        }
    }

    fun resetState() {
        _uiState.value = CreateNoticeUiState.Initial
    }
}

sealed class CreateNoticeUiState {
    object Initial : CreateNoticeUiState()
    object Loading : CreateNoticeUiState()
    data class Success(val notice: Notice) : CreateNoticeUiState()
    data class Error(val message: String) : CreateNoticeUiState()
}
