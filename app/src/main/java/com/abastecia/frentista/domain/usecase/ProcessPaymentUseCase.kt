package com.abastecia.frentista.domain.usecase

import com.abastecia.frentista.data.model.PaymentResult
import com.abastecia.frentista.data.repository.OrderRepository
import com.abastecia.frentista.data.repository.PlugPagRepository
import javax.inject.Inject

class ProcessPaymentUseCase @Inject constructor(
    private val plugPagRepository: PlugPagRepository,
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(
        orderId: String,
        valueCents: Int,
        type: Int = PlugPagRepository.TYPE_DEBITO,
        installments: Int = 1
    ): PaymentResult {
        val result = plugPagRepository.doPayment(valueCents, type, installments)
        if (result.success) {
            orderRepository.updateAfterMachinePayment(
                orderId = orderId,
                nsu = result.nsu,
                authCode = result.authCode,
                cardLast4 = result.cardLast4,
                installments = result.installments
            )
        }
        return result
    }
}
