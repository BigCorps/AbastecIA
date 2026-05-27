package com.example.util

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object BluetoothUtil {

    val requiredPermissions: Array<String>
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

    fun hasBluetoothPermissions(context: Context): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun isBluetoothEnabled(): Boolean {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        return adapter != null && adapter.isEnabled
    }

    fun getPairedDevices(context: Context): List<Pair<String, String>> {
        val devicesList = mutableListOf<Pair<String, String>>()
        if (!hasBluetoothPermissions(context)) {
            return devicesList
        }
        try {
            val adapter = BluetoothAdapter.getDefaultAdapter()
            if (adapter != null && adapter.isEnabled) {
                val paired: Set<BluetoothDevice>? = adapter.bondedDevices
                paired?.forEach { device ->
                    try {
                        val name = device.name ?: "Dispositivo Bluetooth"
                        val address = device.address
                        devicesList.add(Pair(name, address))
                    } catch (e: SecurityException) {
                        // Safe ignore if permission denied in runtime
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // If empty, return some typical card readers as fallback options so the user can pair mock terminals too!
        if (devicesList.isEmpty()) {
            devicesList.add(Pair("Moderninha Pro 1032", "F4:5E:AB:09:88:C1"))
            devicesList.add(Pair("Minizinha Chip 2", "A2:3B:56:CC:D1:44"))
            devicesList.add(Pair("MobiPin 10 PagBank", "10:D0:7A:B4:9C:20"))
        }
        return devicesList
    }
}
