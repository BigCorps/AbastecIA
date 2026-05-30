package com.abastecia.frentista.presentation.ui.painel

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.abastecia.frentista.data.model.FuelOrder
import com.abastecia.frentista.data.repository.PlugPagRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PainelScreen(
    onNavigateToConfig: () -> Unit,
    viewModel: PainelViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showPaymentDialog by remember { mutableStateOf<FuelOrder?>(null) }
    var showDirectSaleDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("abastecIA — Painel do Frentista") },
                actions = {
                    IconButton(onClick = onNavigateToConfig) {
                        Icon(Icons.Default.Settings, contentDescription = "Configurações")
                    }
                    Icon(
                        imageVector = if (state.isConnected)
                            Icons.Default.WifiTethering else Icons.Default.WifiOff,
                        contentDescription = "Conexão",
                        tint = if (state.isConnected) Color(0xFF22C55E) else Color(0xFFEF4444)
                    )
                    Spacer(Modifier.width(12.dp))
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showDirectSaleDialog = true },
                icon = { Icon(Icons.Default.Add, "Venda direta") },
                text = { Text("Venda Direta") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                state.orders.isEmpty() -> Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.LocalGasStation,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(16.dp))
                    Text("Nenhum pedido aguardando",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.orders, key = { it.id }) { order ->
                        OrderCard(
                            order = order,
                            isProcessing = state.processingOrderId == order.id,
                            onCharge = { showPaymentDialog = order },
                            onDone = { viewModel.onEvent(PainelEvent.MarkDone(order.id)) }
                        )
                    }
                }
            }

            state.errorMessage?.let { msg ->
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    action = {
                        TextButton(onClick = {
                            viewModel.onEvent(PainelEvent.DismissError)
                        }) { Text("OK") }
                    }
                ) { Text(msg) }
            }

            state.successMessage?.let { msg ->
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    containerColor = Color(0xFF22C55E),
                    action = {
                        TextButton(onClick = {
                            viewModel.onEvent(PainelEvent.DismissSuccess)
                        }) { Text("OK", color = Color.White) }
                    }
                ) { Text(msg, color = Color.White) }
            }
        }
    }

    showPaymentDialog?.let { order ->
        PaymentTypeDialog(
            order = order,
            onConfirm = { type, installments ->
                viewModel.onEvent(PainelEvent.ChargeCard(order, type, installments))
                showPaymentDialog = null
            },
            onDismiss = { showPaymentDialog = null }
        )
    }

    if (showDirectSaleDialog) {
        DirectSaleDialog(
            onConfirm = { pump, fuel, amount, paymentMethod, installments ->
                viewModel.onEvent(PainelEvent.CreateDirectSale(pump, fuel, amount, paymentMethod, installments))
                showDirectSaleDialog = false
            },
            onDismiss = { showDirectSaleDialog = false }
        )
    }
}

@Composable
fun OrderCard(
    order: FuelOrder,
    isProcessing: Boolean,
    onCharge: () -> Unit,
    onDone: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Bomba ${order.pumpNumber}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = when (order.status) {
                        "paid"         -> Color(0xFFFEF3C7)
                        "paid_machine" -> Color(0xFFD1FAE5)
                        else           -> Color(0xFFF3F4F6)
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = when (order.status) {
                            "paid"         -> "Aguardando cobrança"
                            "paid_machine" -> "Pago — Abastecer"
                            else           -> order.status
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(order.fuelType, style = MaterialTheme.typography.bodyLarge)
            Text(
                "R$ %.2f".format(order.amount),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            order.plate?.let {
                Text("Placa: $it", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(12.dp))

            when (order.status) {
                "paid" -> Button(
                    onClick = onCharge,
                    enabled = !isProcessing,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Default.CreditCard, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Cobrar no Cartão")
                    }
                }

                "paid_machine" -> Button(
                    onClick = onDone,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF22C55E)
                    )
                ) {
                    Icon(Icons.Default.LocalGasStation, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Concluir Abastecimento")
                }
            }
        }
    }
}

@Composable
fun PaymentTypeDialog(
    order: FuelOrder,
    onConfirm: (type: Int, installments: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedType by remember { mutableIntStateOf(PlugPagRepository.TYPE_DEBITO) }
    var installments by remember { mutableIntStateOf(1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Forma de Pagamento") },
        text = {
            Column {
                Text("Bomba ${order.pumpNumber} — R$ %.2f".format(order.amount))
                Spacer(Modifier.height(16.dp))

                Text("Tipo:", style = MaterialTheme.typography.labelLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedType == PlugPagRepository.TYPE_DEBITO,
                        onClick = { selectedType = PlugPagRepository.TYPE_DEBITO }
                    )
                    Text("Débito")
                    Spacer(Modifier.width(16.dp))
                    RadioButton(
                        selected = selectedType == PlugPagRepository.TYPE_CREDITO,
                        onClick = { selectedType = PlugPagRepository.TYPE_CREDITO }
                    )
                    Text("Crédito")
                }

                if (selectedType == PlugPagRepository.TYPE_CREDITO) {
                    Spacer(Modifier.height(8.dp))
                    Text("Parcelas:", style = MaterialTheme.typography.labelLarge)
                    listOf(1, 2, 3, 6, 12).forEach { n ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = installments == n,
                                onClick = { installments = n }
                            )
                            Text(if (n == 1) "À vista" else "${n}x")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedType, installments) }) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun DirectSaleDialog(
    onConfirm: (pump: String, fuel: String, amount: Double, paymentMethod: String, installments: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var pumpNumber by remember { mutableStateOf("") }
    var fuelType by remember { mutableStateOf("Gasolina Comum") }
    var amountText by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("card_debit") }
    var installments by remember { mutableIntStateOf(1) }

    val fuelTypes = listOf("Gasolina Comum", "Gasolina Aditivada", "Etanol", "Diesel S10")
    val paymentMethods = listOf(
        "card_debit" to "Cartão de Débito",
        "card_credit" to "Cartão de Crédito",
        "pix" to "Pix",
        "cash" to "Dinheiro"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nova Venda Direta", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = pumpNumber,
                    onValueChange = { pumpNumber = it },
                    label = { Text("Número da Bomba") },
                    placeholder = { Text("Ex: 2") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Valor (R$)") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Combustível:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    fuelTypes.forEach { fuel ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                        ) {
                            RadioButton(
                                selected = fuelType == fuel,
                                onClick = { fuelType = fuel }
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(fuel, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                Text("Forma de Pagamento:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    paymentMethods.forEach { (method, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                        ) {
                            RadioButton(
                                selected = paymentMethod == method,
                                onClick = { paymentMethod = method }
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                if (paymentMethod == "card_credit") {
                    Text("Parcelas:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(1, 2, 3, 6, 12).forEach { n ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                            ) {
                                RadioButton(
                                    selected = installments == n,
                                    onClick = { installments = n }
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(if (n == 1) "À vista" else "${n}x", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            val amount = amountText.toDoubleOrNull() ?: 0.0
            Button(
                onClick = {
                    onConfirm(pumpNumber, fuelType, amount, paymentMethod, installments)
                },
                enabled = pumpNumber.isNotBlank() && amount > 0.0
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
