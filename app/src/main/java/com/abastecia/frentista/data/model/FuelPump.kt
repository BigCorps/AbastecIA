package com.abastecia.frentista.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FuelPump(
    val id: String,
    @SerialName("company_id") val companyId: String,
    val number: String,
    val label: String? = null,
    @SerialName("fuel_types") val fuelTypes: List<String> = emptyList(),
    val status: String = "available"
)
