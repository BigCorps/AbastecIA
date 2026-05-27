package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.screens.ConfigViewModel
import com.example.ui.screens.MainAppNavigation
import com.example.ui.screens.PagamentoViewModel
import com.example.ui.screens.PainelViewModel
import com.example.ui.screens.ViewModelProviderFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as AbasteciaApp
        val factory = ViewModelProviderFactory(app)

        val painelViewModel: PainelViewModel by viewModels { factory }
        val configViewModel: ConfigViewModel by viewModels { factory }
        val pagamentoViewModel: PagamentoViewModel by viewModels { factory }

        setContent {
            MyApplicationTheme {
                MainAppNavigation(
                    painelViewModel = painelViewModel,
                    configViewModel = configViewModel,
                    pagamentoViewModel = pagamentoViewModel
                )
            }
        }
    }
}
