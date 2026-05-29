package com.abastecia.frentista

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.abastecia.frentista.data.preferences.AppPreferences
import com.abastecia.frentista.presentation.navigation.AppNavigation
import com.abastecia.frentista.presentation.navigation.Screen
import com.abastecia.frentista.presentation.ui.theme.AbasteciaTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var preferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val supabaseUrl = runBlocking { preferences.supabaseUrl.first() }
        val companyId   = runBlocking { preferences.companyId.first() }

        // Só vai para o Painel se tiver URL real configurada
        // URL dummy ou vazia = ir para Config primeiro
        val isConfigured = companyId.isNotBlank()
            && supabaseUrl.isNotBlank()
            && !supabaseUrl.contains("dummy")
            && supabaseUrl.startsWith("https://")
            && !supabaseUrl.contains("dummy.supabase.co")

        val startDest = if (isConfigured) Screen.Painel.route else Screen.Config.route

        setContent {
            AbasteciaTheme {
                AppNavigation(startDestination = startDest)
            }
        }
    }
}
