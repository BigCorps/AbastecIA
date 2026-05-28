package com.abastecia.frentista.presentation.ui.config

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ConfigScreen(
    onSaved: () -> Unit,
    viewModel: ConfigViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Configurações do Terminal",
            style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = state.supabaseUrl,
            onValueChange = viewModel::onUrlChange,
            label = { Text("URL do Supabase") },
            placeholder = { Text("https://xxx.supabase.co") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = state.supabaseKey,
            onValueChange = viewModel::onKeyChange,
            label = { Text("Anon Key") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = state.companyId,
            onValueChange = viewModel::onCompanyIdChange,
            label = { Text("ID do Posto (company_id)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = state.pumpNumber,
            onValueChange = viewModel::onPumpNumberChange,
            label = { Text("Número desta Bomba") },
            placeholder = { Text("01") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Button(
            onClick = { viewModel.save(onSaved) },
            modifier = Modifier.fillMaxWidth(),
            enabled = state.isValid
        ) {
            Text("Salvar e Conectar")
        }
    }
}
