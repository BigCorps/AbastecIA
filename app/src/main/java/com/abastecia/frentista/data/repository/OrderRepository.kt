package com.abastecia.frentista.data.repository

import com.abastecia.frentista.data.api.SupabaseProvider
import com.abastecia.frentista.data.model.FuelOrder
import com.abastecia.frentista.presentation.ui.debug.AppLogger
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.launch
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository @Inject constructor(
    private val supabaseProvider: SupabaseProvider
) {
    // Buscar pedidos pagos aguardando cobrança na maquininha
    suspend fun getPaidOrders(companyId: String): List<FuelOrder> {
        try {
            val supabase = supabaseProvider.getClient()
            val result = supabase.from("fuel_orders")
                .select {
                    filter {
                        eq("company_id", companyId)
                        eq("status", "paid")
                    }
                }
            AppLogger.d("OrderRepo", "Raw JSON: ${result.data}")
            val list = result.decodeList<FuelOrder>()
            AppLogger.d("OrderRepo", "Decoded ${list.size} orders")
            return list
        } catch (e: Exception) {
            AppLogger.e("OrderRepo", "ERRO: ${e.message ?: "desconhecido"}")
            throw e
        }
    }

    // Escutar novos pedidos em tempo real
    fun observeOrders(companyId: String): Flow<List<FuelOrder>> = callbackFlow {
        AppLogger.d("OrderRepo", "Iniciando canal para: $companyId")

        // Buscar dados iniciais
        val initial = getPaidOrders(companyId)
        AppLogger.d("OrderRepo", "Dados iniciais: ${initial.size} pedidos")
        trySend(initial)

        val supabase = supabaseProvider.getClient()
        // Garantir que a conexão do websockets em tempo real esteja ativa
        supabase.realtime.connect()
        AppLogger.d("OrderRepo", "Conexão Realtime iniciada")

        // Criar canal Realtime
        val channel = supabase.channel("fuel_$companyId")

        val changeFlow = channel.postgresChangeFlow<PostgresAction>(
            schema = "public"
        ) {
            table = "fuel_orders"
            filter = "company_id=eq.$companyId"
        }

        // Processar mudanças em coroutine separada
        val job = changeFlow.onEach { action ->
            AppLogger.d("OrderRepo", "Mudança detectada: ${action::class.simpleName}")
            val updated = getPaidOrders(companyId)
            AppLogger.d("OrderRepo", "Após mudança: ${updated.size} pedidos")
            trySend(updated)
        }.launchIn(this)

        // Subscrever APÓS configurar o listener
        channel.subscribe()
        AppLogger.d("OrderRepo", "Canal subscrito com sucesso")

        // Manter o Flow aberto até ser cancelado
        awaitClose {
            AppLogger.d("OrderRepo", "Fechando canal")
            job.cancel()
            launch { supabase.realtime.removeChannel(channel) }
        }
    }

    // Atualizar status após pagamento na maquininha
    suspend fun updateAfterMachinePayment(
        orderId: String,
        nsu: String?,
        authCode: String?,
        cardLast4: String?,
        installments: Int
    ) {
        AppLogger.d("OrderRepo", "Atualizando pedido $orderId após sucesso no PlugPag (NSU: $nsu)")
        val supabase = supabaseProvider.getClient()
        supabase.from("fuel_orders").update({
            set("status", "paid_machine")
            set("plugpag_nsu", nsu)
            set("plugpag_auth", authCode)
            set("plugpag_card_last4", cardLast4)
            set("plugpag_installments", installments)
            set("paid_at", kotlinx.datetime.Clock.System.now().toString())
        }) {
            filter { eq("id", orderId) }
        }
        AppLogger.d("OrderRepo", "Pedido $orderId atualizado com sucesso no banco")
    }

    // Finalizar abastecimento
    suspend fun markAsDone(orderId: String) {
        AppLogger.d("OrderRepo", "Finalizando abastecimento para pedido $orderId")
        val supabase = supabaseProvider.getClient()
        supabase.from("fuel_orders").update({
            set("status", "done")
        }) {
            filter { eq("id", orderId) }
        }
        AppLogger.d("OrderRepo", "Pedido $orderId finalizado com sucesso")
    }
}
