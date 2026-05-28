package com.abastecia.frentista.data.model

data class PaymentResult(
    val success: Boolean,
    val nsu: String? = null,
    val authCode: String? = null,
    val cardLast4: String? = null,
    val installments: Int = 1,
    val errorMessage: String? = null
)
