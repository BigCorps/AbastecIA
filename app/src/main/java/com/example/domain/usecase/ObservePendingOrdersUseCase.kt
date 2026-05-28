package com.example.domain.usecase

import com.example.data.model.FuelOrder
import com.example.data.remote.FuelOrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObservePendingOrdersUseCase(
    private val repository: FuelOrderRepository
) {
    fun execute(companyId: String): Flow<List<FuelOrder>> {
        return repository.orders.map { orders ->
            orders.filter { it.company_id == companyId && (it.status == "paid" || it.status == "paid_client_app") }
        }
    }
}
