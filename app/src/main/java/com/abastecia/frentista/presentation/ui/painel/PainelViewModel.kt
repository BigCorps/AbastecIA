package com.abastecia.frentista.presentation.ui.painel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abastecia.frentista.data.model.FuelOrder
import com.abastecia.frentista.data.preferences.AppPreferences
import com.abastecia.frentista.data.repository.OrderRepository
import com.abastecia.frentista.domain.usecase.ObservePaidOrdersUseCase
import com.abastecia.frentista.domain.usecase.ProcessPaymentUseCase
import com.abastecia.frentista.data.repository.PlugPagRepository
import com.abastecia.frentista.data.repository.IPlugPagRepository
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
    data class CreateDirectSale(
        val pumpNumber: String,
        val fuelType: String,
        val amount: Double,
        val paymentMethod: String, // "card_debit", "card_credit", "pix", "cash"
        val installments: Int = 1
    ) : PainelEvent()
    data class MarkDone(val orderId: String) : PainelEvent()
    object DismissError : PainelEvent()
    object DismissSuccess : PainelEvent()
}

@HiltViewModel
class PainelViewModel @Inject constructor(
    private val observePaidOrders: ObservePaidOrdersUseCase,
    private val processPayment: ProcessPaymentUseCase,
    private val plugPagRepository: IPlugPagRepository,
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
            is PainelEvent.CreateDirectSale -> createDirectSale(
                event.pumpNumber,
                event.fuelType,
                event.amount,
                event.paymentMethod,
                event.installments
            )
            is PainelEvent.MarkDone  -> markDone(event.orderId)
            is PainelEvent.DismissError -> _uiState.update { it.copy(errorMessage = null) }
            is PainelEvent.DismissSuccess -> _uiState.update { it.copy(successMessage = null) }
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

    private fun createDirectSale(
        pumpNumber: String,
        fuelType: String,
        amount: Double,
        paymentMethod: String,
        installments: Int
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val companyId = preferences.companyId.first()
            val uniqueId = java.util.UUID.randomUUID().toString()
            val now = kotlinx.datetime.Clock.System.now().toString()

            var success = true
            var nsu: String? = null
            var authCode: String? = null
            var cardLast4: String? = null
            var errorMessage: String? = null

            if (paymentMethod == "card_debit" || paymentMethod == "card_credit") {
                val plugpagType = if (paymentMethod == "card_credit") PlugPagRepository.TYPE_CREDITO else PlugPagRepository.TYPE_DEBITO
                val valueCents = (amount * 100).toInt()
                AppLogger.d("PainelVM", "Processando venda direta $paymentMethod de R$ $amount na maquininha...")
                val result = plugPagRepository.doPayment(valueCents, plugpagType, installments)
                if (result.success) {
                    nsu = result.nsu
                    authCode = result.authCode
                    cardLast4 = result.cardLast4
                    AppLogger.d("PainelVM", "Venda direta aprovada na maquininha! NSU: $nsu")
                } else {
                    success = false
                    errorMessage = result.errorMessage
                    AppLogger.d("PainelVM", "Venda direta rejeitada: $errorMessage")
                }
            } else if (paymentMethod == "pix") {
                kotlinx.coroutines.delay(1000)
                nsu = "PIX${System.currentTimeMillis()}"
                AppLogger.d("PainelVM", "Venda direta Pix registrada!")
            } else {
                kotlinx.coroutines.delay(600)
                nsu = "CASH${System.currentTimeMillis()}"
                AppLogger.d("PainelVM", "Venda direta em Dinheiro registrada!")
            }

            if (success) {
                try {
                    val order = FuelOrder(
                        id = uniqueId,
                        companyId = companyId,
                        pumpNumber = pumpNumber,
                        fuelType = fuelType,
                        amount = amount,
                        status = "paid_machine",
                        plate = null,
                        paymentMethod = paymentMethod,
                        plugpagNsu = nsu,
                        plugpagAuth = authCode,
                        plugpagCardLast4 = cardLast4,
                        plugpagInstallments = installments,
                        paidAt = now,
                        createdAt = now
                    )
                    orderRepository.createDirectOrder(order)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Venda direta registrada com sucesso!"
                        )
                    }
                } catch (e: Exception) {
                    AppLogger.e("PainelVM", "Erro ao salvar venda direta no banco: ${e.message}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Erro ao gravar venda no banco: ${e.message}"
                        )
                    }
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = errorMessage ?: "Falha na transação da maquininha"
                    )
                }
            }
        }
    }

    private fun markDone(orderId: String) {
        viewModelScope.launch {
            orderRepository.markAsDone(orderId)
        }
    }
}
