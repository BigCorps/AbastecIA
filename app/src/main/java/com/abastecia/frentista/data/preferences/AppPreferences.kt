package com.abastecia.frentista.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.abastecia.frentista.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore by preferencesDataStore(name = "abastecia_prefs")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val SUPABASE_URL = stringPreferencesKey("supabase_url")
        val SUPABASE_KEY = stringPreferencesKey("supabase_key")
        val COMPANY_ID   = stringPreferencesKey("company_id")
        val PUMP_NUMBER  = stringPreferencesKey("pump_number")
    }

    val supabaseUrl: Flow<String> = context.dataStore.data
        .map { it[SUPABASE_URL] ?: BuildConfig.SUPABASE_URL }

    val supabaseKey: Flow<String> = context.dataStore.data
        .map { it[SUPABASE_KEY] ?: BuildConfig.SUPABASE_ANON_KEY }

    val companyId: Flow<String> = context.dataStore.data
        .map { it[COMPANY_ID] ?: BuildConfig.COMPANY_ID_DEFAULT }

    val pumpNumber: Flow<String> = context.dataStore.data
        .map { it[PUMP_NUMBER] ?: "01" }

    suspend fun save(url: String, key: String, companyId: String, pumpNumber: String) {
        context.dataStore.edit { prefs ->
            prefs[SUPABASE_URL]  = url
            prefs[SUPABASE_KEY]  = key
            prefs[COMPANY_ID]    = companyId
            prefs[PUMP_NUMBER]   = pumpNumber
        }
    }
}
