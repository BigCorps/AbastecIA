package com.abastecia.frentista.data.repository

import com.abastecia.frentista.data.model.FuelOrder
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.PostgresAction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
    fun observeOrders(companyId: String): Flow<List<FuelOrder>> {
        val channel = supabase.channel("fuel_orders_$companyId")
        return channel
            .postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "fuel_orders"
                filter = "company_id=eq.$companyId"
            }
            .map { getPaidOrders(companyId) }
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
