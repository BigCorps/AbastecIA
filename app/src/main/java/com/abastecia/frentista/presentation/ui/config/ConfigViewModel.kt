package com.abastecia.frentista.presentation.ui.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abastecia.frentista.BuildConfig
import com.abastecia.frentista.data.preferences.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConfigUiState(
    val companyId: String = "",
    val pumpNumber: String = "01"
) {
    val isValid get() = companyId.isNotBlank()
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
                preferences.companyId,
                preferences.pumpNumber
            ) { company, pump ->
                ConfigUiState(company, pump)
            }.collect { _uiState.value = it }
        }
    }

    fun onCompanyIdChange(v: String) { _uiState.update { it.copy(companyId = v) } }
    fun onPumpNumberChange(v: String){ _uiState.update { it.copy(pumpNumber = v) } }

    fun save(onSaved: () -> Unit) {
        viewModelScope.launch {
            val s = _uiState.value
            // Salvar apenas company_id e pump_number
            // URL e Key vêm do BuildConfig
            preferences.save(
                url = BuildConfig.SUPABASE_URL,
                key = BuildConfig.SUPABASE_ANON_KEY,
                companyId = s.companyId,
                pumpNumber = s.pumpNumber
            )
            onSaved()
        }
    }
}
