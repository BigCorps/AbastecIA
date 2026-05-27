package com.example.domain.usecase

import com.example.data.remote.FuelOrderRepository
import com.example.data.remote.PlugPagRepository
import br.com.uol.pagseguro.plugpag.PlugPag
import br.com.uol.pagseguro.plugpag.PlugPagPaymentResult

class ProcessPaymentUseCase(
    private val plugPagRepository: PlugPagRepository,
    private val fuelOrderRepository: FuelOrderRepository
) {
    suspend fun execute(
        orderId: String,
        amountCents: Int,
        paymentType: Int = PlugPag.TYPE_CREDITO,
        installments: Int = 1
    ): Result<PlugPagPaymentResult> {
        return try {
            val result = plugPagRepository.doPayment(
                valueCents = amountCents,
                type = paymentType,
                installments = installments
            )
            
            if (result.resultCode == PlugPag.RET_OK) {
                fuelOrderRepository.updateOrderStatus(
                    orderId = orderId,
                    status = "paid_machine",
                    nsu = result.nsu,
                    auth = result.authorizationCode,
                    cardLast4 = result.cardNumber.takeLast(4),
                    installments = installments
                )
                Result.success(result)
            } else {
                Result.failure(Exception(result.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
