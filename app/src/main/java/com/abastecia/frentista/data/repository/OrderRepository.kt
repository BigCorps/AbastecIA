package com.abastecia.frentista.data.repository

import com.abastecia.frentista.data.model.FuelOrder
import io.github.jan.supabase.SupabaseClient
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
    private val supabase: SupabaseClient
) {
    // Buscar pedidos pagos aguardando cobrança na maquininha
    suspend fun getPaidOrders(companyId: String): List<FuelOrder> =
        supabase.from("fuel_orders")
            .select {
                filter {
                    eq("company_id", companyId)
                    eq("status", "paid")
                }
            }
            .decodeList()

    // Escutar novos pedidos em tempo real
    fun observeOrders(companyId: String): Flow<List<FuelOrder>> = callbackFlow {
        // Buscar dados iniciais imediatamente
        val initial = getPaidOrders(companyId)
        trySend(initial)

        // Criar e subscrever o channel
        val channel = supabase.channel("fuel_orders_$companyId") {
            // Sem configuração extra necessária
        }

        channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "fuel_orders"
            filter = "company_id=eq.$companyId"
        }.onEach {
            // A cada mudança, rebuscar a lista atualizada
            val updated = getPaidOrders(companyId)
            trySend(updated)
        }.launchIn(this)

        // CRÍTICO: subscrever o channel
        channel.subscribe()

        // Limpar ao fechar o Flow
        awaitClose {
            launch(NonCancellable) {
                supabase.realtime.removeChannel(channel)
            }
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
    }

    // Finalizar abastecimento
    suspend fun markAsDone(orderId: String) {
        supabase.from("fuel_orders").update({
            set("status", "done")
        }) {
            filter { eq("id", orderId) }
        }
    }
}
