package com.abastecia.frentista.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FuelOrder(
    val id: String,
    @SerialName("company_id") val companyId: String,
    @SerialName("pump_number") val pumpNumber: String,
    @SerialName("fuel_type") val fuelType: String,
    val amount: Double,
    val status: String,
    val plate: String? = null,
    @SerialName("payment_method") val paymentMethod: String = "card",
    @SerialName("plugpag_nsu") val plugpagNsu: String? = null,
    @SerialName("plugpag_auth") val plugpagAuth: String? = null,
    @SerialName("plugpag_card_last4") val plugpagCardLast4: String? = null,
    @SerialName("plugpag_installments") val plugpagInstallments: Int = 1,
    @SerialName("paid_at") val paidAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)
