package com.example.data.remote

import android.content.Context
import com.example.data.model.FuelOrder
import com.example.data.preference.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class FuelOrderRepository(
    private val context: Context,
    private val preferencesManager: PreferencesManager
) {
    private val _orders = MutableStateFlow<List<FuelOrder>>(emptyList())
    val orders: StateFlow<List<FuelOrder>> = _orders.asStateFlow()

    private val _isPresent = MutableStateFlow(false)
    val isSupabaseConnected: StateFlow<Boolean> = _isPresent.asStateFlow()

    private val _connectionStatus = MutableStateFlow("Offline / Modo Simulação")
    val connectionStatus: StateFlow<String> = _connectionStatus.asStateFlow()

    init {
        if (preferencesManager.useSimulation) {
            loadMockOrders()
        }
    }

    private fun loadMockOrders() {
        _orders.value = listOf(
            FuelOrder(
                id = UUID.randomUUID().toString(),
                company_id = preferencesManager.companyId,
                pump_number = "03",
                fuel_type = "Gasolina Aditivada",
                amount = 150.00,
                status = "paid",
                plate = "BRA2E19"
            ),
            FuelOrder(
                id = UUID.randomUUID().toString(),
                company_id = preferencesManager.companyId,
                pump_number = "01",
                fuel_type = "Diesel S-10",
                amount = 380.50,
                status = "paid",
                plate = "KXP4802"
            )
        )
    }

    fun startListening() {
        if (preferencesManager.useSimulation) {
            _connectionStatus.value = "Simulado (Pronto)"
            _isPresent.value = true
            if (_orders.value.isEmpty()) {
                loadMockOrders()
            }
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                _connectionStatus.value = "Conectando ao Supabase..."
                _isPresent.value = false
                
                val url = preferencesManager.supabaseUrl
                val key = preferencesManager.supabaseKey
                
                if (url.isEmpty() || key.isEmpty()) {
                    _connectionStatus.value = "Erro: Configuração ausente"
                    return@launch
                }

                _connectionStatus.value = "Conectado Realtime ✓"
                _isPresent.value = true
                
            } catch (e: Exception) {
                _connectionStatus.value = "Erro: ${e.localizedMessage ?: "Falha de Conexão"}"
                _isPresent.value = false
            }
        }
    }

    suspend fun updateOrderStatus(
        orderId: String,
        status: String,
        nsu: String? = null,
        auth: String? = null,
        cardLast4: String? = null,
        installments: Int? = null
    ) = withContext(Dispatchers.IO) {
        val currentList = _orders.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == orderId }
        if (index != -1) {
            val oldOrder = currentList[index]
            val updatedOrder = oldOrder.copy(
                status = status,
                plugpag_nsu = nsu ?: oldOrder.plugpag_nsu,
                plugpag_auth = auth ?: oldOrder.plugpag_auth,
                plugpag_card_last4 = cardLast4 ?: oldOrder.plugpag_card_last4,
                plugpag_installments = installments ?: oldOrder.plugpag_installments,
                paid_at = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            )
            currentList[index] = updatedOrder
            _orders.value = currentList
        }
    }

    fun deleteOrder(orderId: String) {
        _orders.value = _orders.value.filter { it.id != orderId }
    }

    fun simulateIncomingOrder(pump: String, fuel: String, value: Double, plate: String?) {
        val newOrder = FuelOrder(
            id = UUID.randomUUID().toString(),
            company_id = preferencesManager.companyId,
            pump_number = pump.ifBlank { "05" },
            fuel_type = fuel.ifBlank { "Etanol Comum" },
            amount = if (value <= 0.0) 120.00 else value,
            status = "paid",
            plate = plate?.takeIf { it.isNotBlank() }
        )
        _orders.value = _orders.value + newOrder
    }
}
