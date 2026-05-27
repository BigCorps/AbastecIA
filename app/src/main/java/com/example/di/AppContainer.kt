package com.example.di

import android.content.Context
import com.example.data.preference.PreferencesManager
import com.example.data.remote.FuelOrderRepository
import com.example.data.remote.PlugPagRepository
import com.example.domain.usecase.ObservePendingOrdersUseCase
import com.example.domain.usecase.ProcessPaymentUseCase

class AppContainer(private val context: Context) {
    val preferencesManager = PreferencesManager(context)
    val fuelOrderRepository = FuelOrderRepository(context, preferencesManager)
    val plugPagRepository = PlugPagRepository(context)

    val observePendingOrdersUseCase = ObservePendingOrdersUseCase(fuelOrderRepository)
    val processPaymentUseCase = ProcessPaymentUseCase(plugPagRepository, fuelOrderRepository)
}
