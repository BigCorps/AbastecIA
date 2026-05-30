package com.abastecia.frentista

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.abastecia.frentista.data.preferences.AppPreferences
import com.abastecia.frentista.presentation.navigation.AppNavigation
import com.abastecia.frentista.presentation.navigation.Screen
import com.abastecia.frentista.presentation.ui.theme.AbasteciaTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var preferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AbasteciaTheme {
                var isConfiguredState by remember { mutableStateOf<Boolean?>(null) }

                LaunchedEffect(Unit) {
                    val companyId = preferences.companyId.first()
                    // Só vai para o Painel se tiver ID do posto configurado e URL no BuildConfig
                    isConfiguredState = companyId.isNotBlank()
                            && BuildConfig.SUPABASE_URL.isNotBlank()
                }

                val startDest = when (isConfiguredState) {
                    null -> null
                    true -> Screen.Painel.route
                    false -> Screen.Config.route
                }

                if (startDest != null) {
                    AppNavigation(startDestination = startDest)
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
