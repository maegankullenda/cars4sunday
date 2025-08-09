package com.maegankullenda.carsonsunday.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maegankullenda.carsonsunday.data.source.DataSourceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataSourceManager: DataSourceManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadCurrentSettings()
    }

    private fun loadCurrentSettings() {
        val isRemote = dataSourceManager.isUsingRemoteStorage()
        println("DEBUG: Current storage setting - Remote: $isRemote")
        _uiState.value = SettingsUiState(
            isUsingRemoteStorage = isRemote,
        )
    }

    fun setUseRemoteStorage(useRemote: Boolean) {
        viewModelScope.launch {
            dataSourceManager.setUseRemoteStorage(useRemote)
            _uiState.value = _uiState.value.copy(isUsingRemoteStorage = useRemote)
        }
    }
}

data class SettingsUiState(
    val isUsingRemoteStorage: Boolean = true, // Default to Firebase remote storage
)
