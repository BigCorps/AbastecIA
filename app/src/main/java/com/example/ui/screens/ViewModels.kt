package com.example.ui.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.AbasteciaApp
import com.example.data.model.FuelOrder
import com.example.data.preference.PreferencesManager
import com.example.data.remote.FuelOrderRepository
import com.example.data.remote.PlugPagRepository
import com.example.domain.usecase.ObservePendingOrdersUseCase
import com.example.domain.usecase.ProcessPaymentUseCase
import br.com.uol.pagseguro.plugpag.PlugPag
import br.com.uol.pagseguro.plugpag.PlugPagEventData
import br.com.uol.pagseguro.plugpag.PlugPagEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.example.util.BluetoothUtil

class ConfigViewModel(
    val preferencesManager: PreferencesManager,
    private val repository: FuelOrderRepository
) : ViewModel() {
    private val _supabaseUrl = MutableStateFlow(preferencesManager.supabaseUrl)
    val supabaseUrl: StateFlow<String> = _supabaseUrl

    private val _supabaseKey = MutableStateFlow(preferencesManager.supabaseKey)
    val supabaseKey: StateFlow<String> = _supabaseKey

    private val _companyId = MutableStateFlow(preferencesManager.companyId)
    val companyId: StateFlow<String> = _companyId

    private val _terminalMac = MutableStateFlow(preferencesManager.terminalMac)
    val terminalMac: StateFlow<String> = _terminalMac

    private val _useSimulation = MutableStateFlow(preferencesManager.useSimulation)
    val useSimulation: StateFlow<Boolean> = _useSimulation

    private val _pairedDevices = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val pairedDevices: StateFlow<List<Pair<String, String>>> = _pairedDevices

    // Login and Profile Management Setup (Secure Backend simulation)
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _selectedProfile = MutableStateFlow("frentista") // "frentista", "caixa", "cliente"
    val selectedProfile: StateFlow<String> = _selectedProfile.asStateFlow()

    private val _userEmail = MutableStateFlow("")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    fun login(email: String, pass: String) {
        _userEmail.value = email
        _isLoggedIn.value = true
        // Automatically fetch database/tenant ID based on user email
        val inferredCompanyId = when {
            email.contains("zona-sul") -> "posto-zona-sul-02"
            email.contains("posto-rio") -> "posto-rio-center"
            else -> "posto-sede-v1"
        }
        updateCompanyId(inferredCompanyId)
    }

    fun logout() {
        _isLoggedIn.value = false
        _userEmail.value = ""
    }

    fun updateSelectedProfile(profile: String) {
        _selectedProfile.value = profile
    }

    fun loadPairedDevices(context: Context) {
        _pairedDevices.value = BluetoothUtil.getPairedDevices(context)
    }

    fun updateSupabaseUrl(url: String) {
        _supabaseUrl.value = url
        preferencesManager.supabaseUrl = url
    }

    fun updateSupabaseKey(key: String) {
        _supabaseKey.value = key
        preferencesManager.supabaseKey = key
    }

    fun updateCompanyId(id: String) {
        _companyId.value = id
        preferencesManager.companyId = id
    }

    fun updateTerminalMac(mac: String) {
        _terminalMac.value = mac
        preferencesManager.terminalMac = mac
    }

    fun updateUseSimulation(sim: Boolean) {
        _useSimulation.value = sim
        preferencesManager.useSimulation = sim
        repository.startListening()
    }
}

class PainelViewModel(
    private val observePendingOrdersUseCase: ObservePendingOrdersUseCase,
    private val repository: FuelOrderRepository,
    val preferencesManager: PreferencesManager
) : ViewModel() {
    private val _pendingOrders = MutableStateFlow<List<FuelOrder>>(emptyList())
    val pendingOrders: StateFlow<List<FuelOrder>> = _pendingOrders

    private val _completedOrders = MutableStateFlow<List<FuelOrder>>(emptyList())
    val completedOrders: StateFlow<List<FuelOrder>> = _completedOrders

    val isSupabaseConnected = repository.isSupabaseConnected
    val connectionStatus = repository.connectionStatus

    init {
        viewModelScope.launch {
            observePendingOrdersUseCase.execute(preferencesManager.companyId).collectLatest {
                _pendingOrders.value = it
            }
        }
        viewModelScope.launch {
            repository.orders.collectLatest { list ->
                _completedOrders.value = list.filter { 
                    it.company_id == preferencesManager.companyId && (it.status == "paid_machine" || it.status == "done") 
                }
            }
        }
    }

    fun refreshConnection() {
        repository.startListening()
    }

    fun executeActionConcluir(orderId: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, "done")
        }
    }

    fun payFromClientApp(orderId: String, isPix: Boolean) {
        viewModelScope.launch {
            val method = if (isPix) "App PIX" else "Saldo App"
            val nsu = "APP-${if (isPix) "PIX" else "SLD"}-${(100000..999999).random()}"
            repository.updateOrderStatus(
                orderId = orderId,
                status = "paid_client_app",
                nsu = nsu,
                auth = "OK_CLIENT_APP",
                cardLast4 = if (isPix) "PIX" else "SLD"
            )
        }
    }

    fun addManualOrder(pump: String, fuel: String, value: Double, plate: String?) {
        repository.simulateIncomingOrder(pump, fuel, value, plate)
    }

    fun removeOrder(id: String) {
        repository.deleteOrder(id)
    }
}

class PagamentoViewModel(
    private val processPaymentUseCase: ProcessPaymentUseCase,
    private val plugPagRepository: PlugPagRepository,
    private val repository: FuelOrderRepository
) : ViewModel() {
    
    sealed class PaymentState {
        object Idle : PaymentState()
        data class Processing(val message: String) : PaymentState()
        data class Approved(
            val nsu: String,
            val auth: String,
            val cardLast4: String,
            val amount: Double
        ) : PaymentState()
        data class Denied(val reason: String) : PaymentState()
    }

    private val _state = MutableStateFlow<PaymentState>(PaymentState.Idle)
    val state: StateFlow<PaymentState> = _state.asStateFlow()

    fun startPayment(orderId: String, amount: Double, paymentType: Int, installments: Int) {
        viewModelScope.launch {
            _state.value = PaymentState.Processing("Iniciando leitor de cartões...")
            
            plugPagRepository.setEventListener(object : PlugPagEventListener {
                override fun onEvent(data: PlugPagEventData) {
                    _state.value = PaymentState.Processing(data.customMessage)
                }
            })

            val amountCents = (amount * 100).toInt()
            val result = processPaymentUseCase.execute(
                orderId = orderId,
                amountCents = amountCents,
                paymentType = paymentType,
                installments = installments
            )

            result.fold(
                onSuccess = { res ->
                    _state.value = PaymentState.Approved(
                        nsu = res.nsu,
                        auth = res.authorizationCode,
                        cardLast4 = res.cardNumber.takeLast(4),
                        amount = amount
                    )
                },
                onFailure = { err ->
                    _state.value = PaymentState.Denied(err.localizedMessage ?: "Erro na comunicação com a maquininha")
                }
            )
        }
    }

    fun resetState() {
        _state.value = PaymentState.Idle
    }
}

class ViewModelProviderFactory(
    private val app: AbasteciaApp
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val container = app.container
        return when {
            modelClass.isAssignableFrom(ConfigViewModel::class.java) -> {
                ConfigViewModel(container.preferencesManager, container.fuelOrderRepository) as T
            }
            modelClass.isAssignableFrom(PainelViewModel::class.java) -> {
                PainelViewModel(container.observePendingOrdersUseCase, container.fuelOrderRepository, container.preferencesManager) as T
            }
            modelClass.isAssignableFrom(PagamentoViewModel::class.java) -> {
                PagamentoViewModel(container.processPaymentUseCase, container.plugPagRepository, container.fuelOrderRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
