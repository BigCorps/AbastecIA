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
import com.abastecia.frentista.presentation.ui.debug.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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

    private var observingJob: Job? = null

    private fun startObserving() {
        viewModelScope.launch {
            // Ler companyId apenas UMA vez — não observar continuamente
            val companyId = preferences.companyId.first()

            if (companyId.isBlank()
                || BuildConfig.SUPABASE_URL.isBlank()
                || BuildConfig.SUPABASE_URL.contains("dummy")) {
                _uiState.update { it.copy(
                    isLoading = false,
                    isConnected = false,
                    errorMessage = "Configure as credenciais do posto primeiro"
                )}
                return@launch
            }

            AppLogger.d("PainelVM", "Iniciando observação para: $companyId")
            _uiState.update { it.copy(isConnected = true) }

            // Iniciar observação uma única vez
            observingJob?.cancel()
            observingJob = viewModelScope.launch {
                observePaidOrders(companyId)
                    .catch { e ->
                        AppLogger.e("PainelVM", "Erro Realtime: ${e.message}")
                        _uiState.update { it.copy(
                            isConnected = false,
                            errorMessage = "Erro de conexão: ${e.message}",
                            isLoading = false
                        )}
                    }
                    .collect { orders ->
                        AppLogger.d("PainelVM", "Recebeu ${orders.size} pedidos")
                        _uiState.update { it.copy(
                            orders = orders,
                            isLoading = false,
                            isConnected = true
                        )}
                    }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        observingJob?.cancel()
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
