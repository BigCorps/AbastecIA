package com.example.data.remote

import android.content.Context
import br.com.uol.pagseguro.plugpag.PlugPag
import br.com.uol.pagseguro.plugpag.PlugPagAppIdentification
import br.com.uol.pagseguro.plugpag.PlugPagEventListener
import br.com.uol.pagseguro.plugpag.PlugPagPaymentData
import br.com.uol.pagseguro.plugpag.PlugPagPaymentResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlugPagRepository(
    private val context: Context
) {
    private val plugPag = PlugPag(
        context,
        PlugPagAppIdentification("AbastecIA", "1.0.0")
    )

    suspend fun doPayment(
        valueCents: Int,
        type: Int = PlugPag.TYPE_CREDITO,
        installments: Int = 1
    ): PlugPagPaymentResult = withContext(Dispatchers.IO) {
        val paymentData = PlugPagPaymentData(
            type = type,
            amount = valueCents,
            installmentType = if (installments > 1) {
                PlugPag.INSTALLMENT_TYPE_PARC_VENDEDOR
            } else {
                PlugPag.INSTALLMENT_TYPE_A_VISTA
            },
            installments = installments,
            userReference = "ABASTECIA-${System.currentTimeMillis()}",
            printReceipt = true
        )
        plugPag.doPayment(paymentData)
    }

    fun setEventListener(listener: PlugPagEventListener) {
        plugPag.setEventListener(listener)
    }
}
