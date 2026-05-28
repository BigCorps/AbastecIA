package com.abastecia.frentista.data.repository

import br.uol.pagseguro.plugpag.PlugPag
import br.uol.pagseguro.plugpag.PlugPagEventListener
import br.uol.pagseguro.plugpag.PlugPagPaymentData
import com.abastecia.frentista.data.model.PaymentResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlugPagRepository @Inject constructor(
    private val plugPag: PlugPag
) {
    companion object {
        const val TYPE_DEBITO  = PlugPag.TYPE_DEBITO
        const val TYPE_CREDITO = PlugPag.TYPE_CREDITO
        const val INSTALLMENT_A_VISTA = PlugPag.INSTALLMENT_TYPE_A_VISTA
        const val INSTALLMENT_PARC    = PlugPag.INSTALLMENT_TYPE_PARC_VENDEDOR
    }

    fun setEventListener(listener: PlugPagEventListener) {
        plugPag.setEventListener(listener)
    }

    suspend fun doPayment(
        valueCents: Int,
        type: Int = TYPE_DEBITO,
        installments: Int = 1
    ): PaymentResult = withContext(Dispatchers.IO) {
        try {
            val data = PlugPagPaymentData(
                type = type,
                amount = valueCents,
                installmentType = if (installments > 1) INSTALLMENT_PARC else INSTALLMENT_A_VISTA,
                installments = installments,
                userReference = "ABASTECIA-${System.currentTimeMillis()}",
                printReceipt = true
            )
            val result = plugPag.doPayment(data)
            if (result.result == 0) {
                PaymentResult(
                    success = true,
                    nsu = result.nsu,
                    authCode = result.authorizationCode,
                    cardLast4 = result.cardNumber?.takeLast(4),
                    installments = installments
                )
            } else {
                PaymentResult(
                    success = false,
                    errorMessage = result.message ?: "Pagamento não aprovado"
                )
            }
        } catch (e: Exception) {
            PaymentResult(success = false, errorMessage = e.message)
        }
    }

    suspend fun voidPayment(nsu: String): PaymentResult = withContext(Dispatchers.IO) {
        try {
            val result = plugPag.voidPayment(
                PlugPagPaymentData(
                    type = PlugPag.TYPE_CREDITO,
                    amount = 0,
                    installmentType = INSTALLMENT_A_VISTA,
                    installments = 1,
                    userReference = nsu,
                    printReceipt = true
                )
            )
            PaymentResult(success = result.result == 0)
        } catch (e: Exception) {
            PaymentResult(success = false, errorMessage = e.message)
        }
    }
}
