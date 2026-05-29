package com.abastecia.frentista.presentation.ui.painel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abastecia.frentista.data.model.FuelOrder
import com.abastecia.frentista.data.preferences.AppPreferences
import com.abastecia.frentista.data.repository.OrderRepository
import com.abastecia.frentista.domain.usecase.ObservePaidOrdersUseCase
import com.abastecia.frentista.domain.usecase.ProcessPaymentUseCase
import com.abastecia.frentista.data.repository.PlugPagRepository
import com.abastecia.frentista.BuildConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PainelUiState(
    val orders: List<FuelOrder> = emptyList(),
    val isLoading: Boolean = true,
    val isConnected: Boolean = false,
    val processingOrderId: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

sealed class PainelEvent {
    data class ChargeCard(val order: FuelOrder, val type: Int, val installments: Int = 1) : PainelEvent()
    data class MarkDone(val orderId: String) : PainelEvent()
    data class DismissError(val unit: Unit = Unit) : PainelEvent()
}

@HiltViewModel
class PainelViewModel @Inject constructor(
    private val observePaidOrders: ObservePaidOrdersUseCase,
    private val processPayment: ProcessPaymentUseCase,
    private val orderRepository: OrderRepository,
    private val preferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(PainelUiState())
    val uiState: StateFlow<PainelUiState> = _uiState.asStateFlow()

    init {
        startObserving()
    }

    private fun startObserving() {
        viewModelScope.launch {
            preferences.companyId.collectLatest { companyId ->
                if (companyId.isBlank()
                    || companyId == BuildConfig.COMPANY_ID_DEFAULT) {
                    // Credenciais não configuradas — parar loading
                    _uiState.update { it.copy(
                        isLoading = false,
                        isConnected = false,
                        errorMessage = "Configure as credenciais do posto primeiro"
                    )}
                    return@collectLatest
                }

                _uiState.update { it.copy(isConnected = true) }

                observePaidOrders(companyId)
                    .catch { e ->
                        _uiState.update { it.copy(
                            isConnected = false,
                            errorMessage = "Erro de conexão: ${e.message}",
                            isLoading = false
                        )}
                    }
                    .collect { orders ->
                        _uiState.update { it.copy(
                            orders = orders,
                            isLoading = false
                        )}
                    }
            }
        }
    }

    fun onEvent(event: PainelEvent) {
        when (event) {
            is PainelEvent.ChargeCard -> chargeCard(event.order, event.type, event.installments)
            is PainelEvent.MarkDone  -> markDone(event.orderId)
            is PainelEvent.DismissError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun chargeCard(order: FuelOrder, type: Int, installments: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(processingOrderId = order.id) }
            val valueCents = (order.amount * 100).toInt()
            val result = processPayment(order.id, valueCents, type, installments)
            _uiState.update {
                it.copy(
                    processingOrderId = null,
                    successMessage = if (result.success) "Aprovado! NSU: ${result.nsu}" else null,
                    errorMessage = if (!result.success) result.errorMessage else null
                )
            }
        }
    }

    private fun markDone(orderId: String) {
        viewModelScope.launch {
            orderRepository.markAsDone(orderId)
        }
    }
}
