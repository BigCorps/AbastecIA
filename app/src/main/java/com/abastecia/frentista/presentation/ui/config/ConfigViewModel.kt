package com.abastecia.frentista.presentation.ui.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abastecia.frentista.data.preferences.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConfigUiState(
    val supabaseUrl: String = "",
    val supabaseKey: String = "",
    val companyId: String = "",
    val pumpNumber: String = "01"
) {
    val isValid get() = supabaseUrl.isNotBlank()
        && supabaseKey.isNotBlank()
        && companyId.isNotBlank()
        && pumpNumber.isNotBlank()
}

@HiltViewModel
class ConfigViewModel @Inject constructor(
    private val preferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConfigUiState())
    val uiState: StateFlow<ConfigUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                preferences.supabaseUrl,
                preferences.supabaseKey,
                preferences.companyId,
                preferences.pumpNumber
            ) { url, key, company, pump ->
                ConfigUiState(url, key, company, pump)
            }.collect { _uiState.value = it }
        }
    }

    fun onUrlChange(v: String)   { _uiState.update { it.copy(supabaseUrl = v) } }
    fun onKeyChange(v: String)   { _uiState.update { it.copy(supabaseKey = v) } }
    fun onCompanyIdChange(v: String) { _uiState.update { it.copy(companyId = v) } }
    fun onPumpNumberChange(v: String){ _uiState.update { it.copy(pumpNumber = v) } }

    fun save(onSaved: () -> Unit) {
        viewModelScope.launch {
            val s = _uiState.value
            preferences.save(s.supabaseUrl, s.supabaseKey, s.companyId, s.pumpNumber)
            onSaved()
        }
    }
}
