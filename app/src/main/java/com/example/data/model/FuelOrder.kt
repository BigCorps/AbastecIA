package com.example.data.model

import kotlinx.serialization.Serializable

@Serializable
data class FuelOrder(
    val id: String,
    val company_id: String,
    val pump_number: String,
    val fuel_type: String,
    val amount: Double,
    var status: String, // "paid", "paid_machine", "done"
    val plate: String? = null,
    val payment_method: String? = "card",
    val plugpag_nsu: String? = null,
    val plugpag_auth: String? = null,
    val plugpag_card_last4: String? = null,
    val plugpag_installments: Int = 1,
    val paid_at: String? = null
)
