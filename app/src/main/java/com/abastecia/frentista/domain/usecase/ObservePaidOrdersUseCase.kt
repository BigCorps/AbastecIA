package com.abastecia.frentista.domain.usecase

import com.abastecia.frentista.data.model.FuelOrder
import com.abastecia.frentista.data.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservePaidOrdersUseCase @Inject constructor(
    private val repository: OrderRepository
) {
    operator fun invoke(companyId: String): Flow<List<FuelOrder>> =
        repository.observeOrders(companyId)
}
