package com.example.data.preference

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("abastecia_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SUPABASE_URL = "supabase_url"
        private const val KEY_SUPABASE_KEY = "supabase_key"
        private const val KEY_COMPANY_ID = "company_id"
        private const val KEY_TERMINAL_MAC = "terminal_mac"
        private const val KEY_USE_SIMULATION = "use_simulation"
    }

    var supabaseUrl: String
        get() = prefs.getString(KEY_SUPABASE_URL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_SUPABASE_URL, value).apply()

    var supabaseKey: String
        get() = prefs.getString(KEY_SUPABASE_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_SUPABASE_KEY, value).apply()

    var companyId: String
        get() = prefs.getString(KEY_COMPANY_ID, "posto-sede-v1") ?: "posto-sede-v1"
        set(value) = prefs.edit().putString(KEY_COMPANY_ID, value).apply()

    var terminalMac: String
        get() = prefs.getString(KEY_TERMINAL_MAC, "F4:5E:AB:09:88:C1") ?: "F4:5E:AB:09:88:C1"
        set(value) = prefs.edit().putString(KEY_TERMINAL_MAC, value).apply()

    var useSimulation: Boolean
        get() = prefs.getBoolean(KEY_USE_SIMULATION, true)
        set(value) = prefs.edit().putBoolean(KEY_USE_SIMULATION, value).apply()

    fun isConfigured(): Boolean {
        return useSimulation || (supabaseUrl.isNotEmpty() && supabaseKey.isNotEmpty())
    }
}
