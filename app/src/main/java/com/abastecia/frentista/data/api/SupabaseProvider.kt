package com.abastecia.frentista.data.api

import com.abastecia.frentista.data.preferences.AppPreferences
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseProvider @Inject constructor(
    private val preferences: AppPreferences
) {
    private var currentClient: SupabaseClient? = null
    private var currentUrl: String? = null
    private var currentKey: String? = null

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    suspend fun getClient(): SupabaseClient {
        val url = preferences.supabaseUrl.first()
        val key = preferences.supabaseKey.first()

        synchronized(this) {
            val client = currentClient
            if (client != null && currentUrl == url && currentKey == key) {
                return client
            }
            // If they changed or it's not initialized yet, rebuild
            try {
                // Try executing close if accessible
                (currentClient as? AutoCloseable)?.close()
            } catch (e: Exception) {
                // Ignore
            }

            val newClient = createSupabaseClient(
                supabaseUrl = url,
                supabaseKey = key
            ) {
                install(Postgrest)
                install(Realtime)
            }
            currentClient = newClient
            currentUrl = url
            currentKey = key
            return newClient
        }
    }
}
