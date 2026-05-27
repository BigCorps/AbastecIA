package com.example.ui.screens

import android.bluetooth.BluetoothAdapter
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import br.com.uol.pagseguro.plugpag.PlugPag
import com.example.data.model.FuelOrder
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// High Density Color Mapping for backward compatibility in Screens
val FuelDarkBg = HighDensityBg
val FuelSurfaceCard = HighDensitySurfaceCard
val FuelNeonTeal = HighDensityPrimary
val FuelNeonBlue = HighDensityPrimary
val FuelPromptYellow = HighDensityBlueBadgeBg
val FuelWarningRed = HighDensityTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppNavigation(
    painelViewModel: PainelViewModel,
    configViewModel: ConfigViewModel,
    pagamentoViewModel: PagamentoViewModel
) {
    var currentTab by remember { mutableStateOf("painel") }
    val context = LocalContext.current

    // Active payment trigger state
    var activePaymentOrder by remember { mutableStateOf<FuelOrder?>(null) }
    var showPaymentDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = HighDensityBg
    ) {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = HighDensitySurfaceCard,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentTab == "painel",
                        onClick = { currentTab = "painel" },
                        icon = { Icon(Icons.Default.LocalGasStation, contentDescription = "Painel") },
                        label = { Text("Painel") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = HighDensityPrimary,
                            selectedTextColor = HighDensityPrimary,
                            unselectedIconColor = HighDensityTextTertiary,
                            unselectedTextColor = HighDensityTextTertiary,
                            indicatorColor = HighDensityBlueBadgeBg
                        )
                    )
                    NavigationBarItem(
                        selected = currentTab == "config",
                        onClick = { currentTab = "config" },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Ajustes") },
                        label = { Text("Ajustes") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = HighDensityPrimary,
                            selectedTextColor = HighDensityPrimary,
                            unselectedIconColor = HighDensityTextTertiary,
                            unselectedTextColor = HighDensityTextTertiary,
                            indicatorColor = HighDensityBlueBadgeBg
                        )
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(HighDensityBg)
            ) {
                when (currentTab) {
                    "painel" -> PainelScreen(
                        viewModel = painelViewModel,
                        configViewModel = configViewModel,
                        onTriggerPayment = { order ->
                            activePaymentOrder = order
                            showPaymentDialog = true
                        }
                    )
                    "config" -> ConfigScreen(
                        viewModel = configViewModel
                    )
                }

                // Interactive Overlaid Payment Dialog (Simulated POS Terminal graphic)
                if (showPaymentDialog && activePaymentOrder != null) {
                    InteractivePaymentDialog(
                        order = activePaymentOrder!!,
                        viewModel = pagamentoViewModel,
                        onDismiss = {
                            showPaymentDialog = false
                            activePaymentOrder = null
                            pagamentoViewModel.resetState()
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PainelScreen(
    viewModel: PainelViewModel,
    configViewModel: ConfigViewModel,
    onTriggerPayment: (FuelOrder) -> Unit
) {
    val pendingOrders by viewModel.pendingOrders.collectAsState()
    val completedOrders by viewModel.completedOrders.collectAsState()
    val isConnected by viewModel.isSupabaseConnected.collectAsState()
    val connStatus by viewModel.connectionStatus.collectAsState()

    var showOrderSimulator by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top App Bar / Connection Status Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, HighDensityOutline, RoundedCornerShape(16.dp))
                .padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(containerColor = HighDensitySurfaceCard)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left side: Logo & Title Layout
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(HighDensityPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "A",
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    }
                    Column {
                        Text(
                            text = "abastecIA",
                            fontWeight = FontWeight.Bold,
                            color = HighDensityTextMain,
                            fontSize = 16.sp,
                            lineHeight = 18.sp
                        )
                        Text(
                            text = "PAINEL DO FRENTISTA",
                            color = HighDensityTextSecondary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 9.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Right side: Compact Badges (NATIVE POS & CLD status)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // NATIVO: Terminal Local Integration Badge
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100))
                            .background(HighDensityBlueBadgeBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(HighDensityPrimary)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "NATIVO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = HighDensityBlueBadgeText
                        )
                    }

                    // CLD: Cloud Realtime Sync Badge
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100))
                            .background(if (isConnected) HighDensityGreenBadgeBg else Color(0xFFFFEBEE))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isConnected) HighDensitySecondary else HighDensityTertiary)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "CLD",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isConnected) HighDensityGreenBadgeText else HighDensityTertiary
                        )
                    }
                }
            }
        }

        // Subtitle & Simulator button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PEDIDOS PAGOS (${pendingOrders.size})",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = HighDensityTextSecondary,
                letterSpacing = 0.5.sp
            )

            Button(
                onClick = { showOrderSimulator = true },
                colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("simulator_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Simular Pedido", fontSize = 12.sp, color = Color.White)
            }
        }

        if (pendingOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(HighDensitySurfaceCard)
                    .border(1.dp, HighDensityOutline, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Inbox,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = HighDensityTextTertiary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Aguardando novos pagamentos do Supabase...",
                        color = HighDensityTextSecondary,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(pendingOrders) { order ->
                    OrderCard(
                        order = order,
                        onPaymentClick = { onTriggerPayment(order) },
                        onRemoveClick = { viewModel.removeOrder(order.id) }
                    )
                }
            }
        }

        // Histórico/Faturados Section
        if (completedOrders.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Faturados & Prontos para Abastecer",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = HighDensityTextSecondary,
                modifier = Modifier.padding(bottom = 8.dp),
                letterSpacing = 0.5.sp
            )

            LazyColumn(
                modifier = Modifier
                    .weight(0.7f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(completedOrders) { order ->
                    CompletedOrderCard(
                        order = order,
                        onConfirmFueling = { viewModel.executeActionConcluir(order.id) },
                        onDeleteClick = { viewModel.removeOrder(order.id) }
                    )
                }
            }
        }
    }

    // Modal Simulation Dialog to add orders
    if (showOrderSimulator) {
        OrderSimulatorDialog(
            onDismiss = { showOrderSimulator = false },
            onSimulate = { pump, fuel, value, plate ->
                viewModel.addManualOrder(pump, fuel, value, plate)
                showOrderSimulator = false
            }
        )
    }
}

@Composable
fun OrderCard(
    order: FuelOrder,
    onPaymentClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, HighDensityOutline, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = HighDensitySurfaceCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Pump Box Indicator (Aesthetic matching Bomba design)
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(HighDensityDoneBg)
                            .border(1.dp, HighDensityOutline, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Text(
                                text = "BOMBA",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = HighDensityTextSecondary
                            )
                            Text(
                                text = order.pump_number,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = HighDensityTextMain
                            )
                        }
                    }

                    // Ticket Info Column
                    Column {
                        Text(
                            text = order.fuel_type,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = HighDensityTextMain
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        if (!order.plate.isNullOrBlank()) {
                            Text(
                                text = "Placa: ${order.plate}",
                                fontSize = 12.sp,
                                color = HighDensityTextSecondary,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            Text(
                                text = "ID: ${order.id.take(8)}",
                                fontSize = 11.sp,
                                color = HighDensityTextTertiary
                            )
                        }
                    }
                }

                // Pricing on Right
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = String.format("R$ %.2f", order.amount),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = HighDensityPrimary
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    val liters = order.amount / 5.79
                    Text(
                        text = String.format("%.2f Litros", liters),
                        fontSize = 10.sp,
                        color = HighDensityTextTertiary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action row inside Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Primary action: Cobrar na Maquininha (First)
                Button(
                    onClick = onPaymentClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("pay_order_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "COBRAR NA MAQUININHA",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Secondary action: Remove/Trash (Later)
                OutlinedButton(
                    onClick = onRemoveClick,
                    modifier = Modifier.height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, HighDensityTertiary.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = HighDensityTertiary),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Excluir", modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun CompletedOrderCard(
    order: FuelOrder,
    onConfirmFueling: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isDone = order.status == "done"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isDone) {
                    Modifier
                        .border(1.dp, HighDensityDoneBorder, RoundedCornerShape(12.dp))
                        .alpha(0.6f)
                } else {
                    Modifier.border(2.dp, HighDensityPrimary, RoundedCornerShape(12.dp))
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isDone) HighDensityDoneBg else HighDensityProcessingBg
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "BOMBA ${order.pump_number}  •  ${order.fuel_type}",
                        fontWeight = FontWeight.Bold,
                        color = if (isDone) HighDensityTextSecondary else HighDensityTextMain,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = String.format("R$ %.2f  |  Card: %s", order.amount, order.plugpag_card_last4 ?: "****"),
                    fontSize = 12.sp,
                    color = if (isDone) HighDensityTextTertiary else HighDensityTextSecondary,
                    fontWeight = FontWeight.Medium
                )
                if (!order.paid_at.isNullOrBlank()) {
                    Text(
                        text = "Pago às: ${order.paid_at}",
                        fontSize = 10.sp,
                        color = HighDensityTextTertiary
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (isDone) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(HighDensityGreenBadgeBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "CONCLUÍDO",
                            color = HighDensityGreenBadgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Deletar", tint = HighDensityTertiary)
                    }
                } else {
                    Button(
                        onClick = onConfirmFueling,
                        colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("complete_fueling_btn"),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "FINALIZAR",
                            fontSize = 11.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(
    viewModel: ConfigViewModel
) {
    val supabaseUrl by viewModel.supabaseUrl.collectAsState()
    val supabaseKey by viewModel.supabaseKey.collectAsState()
    val companyId by viewModel.companyId.collectAsState()
    val useSimulation by viewModel.useSimulation.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Configuração do Dispositivo",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = HighDensityTextMain
            )
            Text(
                text = "Integração Supabase Realtime diretamente no Smart POS PagBank",
                fontSize = 12.sp,
                color = HighDensityTextSecondary
            )
        }

        // Section: Simulation Switch
        item {
            Card(
                modifier = Modifier.border(1.dp, HighDensityOutline, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = HighDensitySurfaceCard)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Demonstração Offline / Simulação", fontWeight = FontWeight.Bold, color = HighDensityTextMain)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "Ative para simular a fila de atendimento da web localmente sem precisar de credenciais Supabase reais.",
                            fontSize = 11.sp,
                            color = HighDensityTextSecondary
                        )
                    }
                    Switch(
                        checked = useSimulation,
                        onCheckedChange = { viewModel.updateUseSimulation(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = HighDensityPrimary,
                            uncheckedThumbColor = HighDensityTextTertiary,
                            uncheckedTrackColor = HighDensityOutline
                        ),
                        modifier = Modifier.testTag("simulation_switch")
                    )
                }
            }
        }

        // Section: Supabase Endpoints (Visual only when Simulation is off, editable always)
        item {
            Card(
                modifier = Modifier.border(1.dp, HighDensityOutline, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = if (useSimulation) HighDensityBg else HighDensitySurfaceCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Parâmetros do Servidor",
                        fontWeight = FontWeight.Bold,
                        color = if (useSimulation) HighDensityTextTertiary else HighDensityTextMain,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = supabaseUrl,
                        onValueChange = { viewModel.updateSupabaseUrl(it) },
                        label = { Text("URL do Banco (Supabase URL)") },
                        modifier = Modifier.fillMaxWidth().testTag("supabase_url"),
                        leadingIcon = { Icon(Icons.Default.Cloud, contentDescription = null) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = HighDensityTextMain,
                            unfocusedTextColor = HighDensityTextSecondary,
                            focusedBorderColor = HighDensityPrimary,
                            unfocusedBorderColor = HighDensityOutline,
                            focusedLabelColor = HighDensityPrimary,
                            unfocusedLabelColor = HighDensityTextSecondary,
                            focusedLeadingIconColor = HighDensityPrimary,
                            unfocusedLeadingIconColor = HighDensityTextSecondary
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = supabaseKey,
                        onValueChange = { viewModel.updateSupabaseKey(it) },
                        label = { Text("Proxy/Anon Public Key") },
                        modifier = Modifier.fillMaxWidth().testTag("supabase_key"),
                        leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = HighDensityTextMain,
                            unfocusedTextColor = HighDensityTextSecondary,
                            focusedBorderColor = HighDensityPrimary,
                            unfocusedBorderColor = HighDensityOutline,
                            focusedLabelColor = HighDensityPrimary,
                            unfocusedLabelColor = HighDensityTextSecondary,
                            focusedLeadingIconColor = HighDensityPrimary,
                            unfocusedLeadingIconColor = HighDensityTextSecondary
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = companyId,
                        onValueChange = { viewModel.updateCompanyId(it) },
                        label = { Text("ID do Posto (company_id)") },
                        modifier = Modifier.fillMaxWidth().testTag("company_id"),
                        leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = HighDensityTextMain,
                            unfocusedTextColor = HighDensityTextSecondary,
                            focusedBorderColor = HighDensityPrimary,
                            unfocusedBorderColor = HighDensityOutline,
                            focusedLabelColor = HighDensityPrimary,
                            unfocusedLabelColor = HighDensityTextSecondary,
                            focusedLeadingIconColor = HighDensityPrimary,
                            unfocusedLeadingIconColor = HighDensityTextSecondary
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun InteractivePaymentDialog(
    order: FuelOrder,
    viewModel: PagamentoViewModel,
    onDismiss: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var paymentType by remember { mutableIntStateOf(PlugPag.TYPE_CREDITO) }
    var installments by remember { mutableIntStateOf(1) }

    Dialog(onDismissRequest = {
        // Prevent dismissal while processing to act like locked POS thread
        if (state !is PagamentoViewModel.PaymentState.Processing) {
            onDismiss()
        }
    }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .border(1.dp, HighDensityOutline, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = HighDensityBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cobrança Direta no Terminal",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = HighDensityTextMain
                    )
                    IconButton(
                        onClick = onDismiss,
                        enabled = state !is PagamentoViewModel.PaymentState.Processing
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = HighDensityTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(HighDensityBlueBadgeBg)
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "Aguardando pagamento de:",
                            fontSize = 11.sp,
                            color = HighDensityBlueBadgeText,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "BOMBA ${order.pump_number} (${order.fuel_type})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = HighDensityBlueBadgeText
                            )
                            Text(
                                text = String.format("R$ %.2f", order.amount),
                                fontWeight = FontWeight.ExtraBold,
                                color = HighDensityPrimary,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (val currentState = state) {
                    is PagamentoViewModel.PaymentState.Idle -> {
                        // Options select
                        Text(
                            "Escolha a Forma de Pagamento",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = HighDensityTextSecondary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { paymentType = PlugPag.TYPE_CREDITO },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (paymentType == PlugPag.TYPE_CREDITO) HighDensityPrimary else HighDensityDoneBg
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("select_credit_btn")
                            ) {
                                Text(
                                    "CRÉDITO",
                                    color = if (paymentType == PlugPag.TYPE_CREDITO) Color.White else HighDensityTextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            Button(
                                onClick = { paymentType = PlugPag.TYPE_DEBITO },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (paymentType == PlugPag.TYPE_DEBITO) HighDensityPrimary else HighDensityDoneBg
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("select_debit_btn")
                            ) {
                                Text(
                                    "DÉBITO",
                                    color = if (paymentType == PlugPag.TYPE_DEBITO) Color.White else HighDensityTextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { paymentType = PlugPag.TYPE_PIX },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (paymentType == PlugPag.TYPE_PIX) HighDensityPrimary else HighDensityDoneBg
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("select_pix_btn")
                            ) {
                                Text(
                                    "PIX",
                                    color = if (paymentType == PlugPag.TYPE_PIX) Color.White else HighDensityTextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            Button(
                                onClick = { paymentType = PlugPag.TYPE_DINHEIRO },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (paymentType == PlugPag.TYPE_DINHEIRO) HighDensityPrimary else HighDensityDoneBg
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("select_cash_btn")
                            ) {
                                Text(
                                    "DINHEIRO",
                                    color = if (paymentType == PlugPag.TYPE_DINHEIRO) Color.White else HighDensityTextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        if (paymentType == PlugPag.TYPE_CREDITO) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = HighDensityTextSecondary)
                                    Text("Parcelas", color = HighDensityTextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { if (installments > 1) installments-- },
                                        enabled = installments > 1
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "menos", tint = HighDensityPrimary)
                                    }
                                    Text(
                                        text = "$installments x",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = HighDensityTextMain,
                                        fontSize = 15.sp
                                    )
                                    IconButton(
                                        onClick = { if (installments < 12) installments++ },
                                        enabled = installments < 12
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "mais", tint = HighDensityPrimary)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                viewModel.startPayment(order.id, order.amount, paymentType, installments)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("start_pos_payment_btn")
                        ) {
                            Text(
                                "INICIAR COBRANÇA",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    is PagamentoViewModel.PaymentState.Processing -> {
                        // Display visual card Moderninha terminal simulation!
                        Card(
                            modifier = Modifier
                                .width(180.dp)
                                .height(260.dp)
                                .padding(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C3238)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Screen of PagBank device
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.White)
                                        .border(2.dp, HighDensityPrimary, RoundedCornerShape(6.dp))
                                        .padding(6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        "PAGBANK SMART",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 9.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = HighDensityPrimary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = currentState.message,
                                        fontSize = 10.sp,
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 12.sp
                                    )
                                }

                                // Keypad design
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Box(Modifier.weight(1f).height(12.dp).background(Color.Gray))
                                        Box(Modifier.weight(1f).height(12.dp).background(Color.Gray))
                                        Box(Modifier.weight(1f).height(12.dp).background(Color.Gray))
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Box(Modifier.weight(1f).height(12.dp).background(Color.Gray))
                                        Box(Modifier.weight(1f).height(12.dp).background(Color.Gray))
                                        Box(Modifier.weight(1f).height(12.dp).background(Color.Gray))
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Box(Modifier.weight(1f).height(12.dp).background(Color.Red))
                                        Box(Modifier.weight(1f).height(12.dp).background(Color.Yellow))
                                        Box(Modifier.weight(1f).height(12.dp).background(Color.Green))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Enviando faturamento na maquininha...",
                            color = HighDensityTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    is PagamentoViewModel.PaymentState.Approved -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = HighDensitySecondary,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "PAGAMENTO APROVADO!",
                                color = HighDensitySecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Display metadata
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(HighDensityDoneBg)
                                    .border(1.dp, HighDensityOutline, RoundedCornerShape(10.dp))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("NSU PagBank", color = HighDensityTextSecondary, fontSize = 12.sp)
                                    Text(currentState.nsu, color = HighDensityTextMain, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Código Aut.", color = HighDensityTextSecondary, fontSize = 12.sp)
                                    Text(currentState.auth, color = HighDensityTextMain, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Cartão final", color = HighDensityTextSecondary, fontSize = 12.sp)
                                    Text("**** **** **** ${currentState.cardLast4}", color = HighDensityTextMain, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onDismiss,
                                colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("dismiss_approved_btn")
                            ) {
                                Text("CONCLUIR", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    is PagamentoViewModel.PaymentState.Denied -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = HighDensityTertiary,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "PAGAMENTO REJEITADO",
                                color = HighDensityTertiary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = currentState.reason,
                                color = HighDensityTextSecondary,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = onDismiss,
                                    colors = ButtonDefaults.buttonColors(containerColor = HighDensityDoneBg),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).testTag("cancel_denied_btn")
                                ) {
                                    Text("FECHAR", color = HighDensityTextSecondary, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { viewModel.resetState() },
                                    colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).testTag("retry_denied_btn")
                                ) {
                                    Text("TENTAR NOVAMENTE", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderSimulatorDialog(
    onDismiss: () -> Unit,
    onSimulate: (String, String, Double, String) -> Unit
) {
    var pump by remember { mutableStateOf("03") }
    var fuel by remember { mutableStateOf("Gasolina Aditivada") }
    var valueTxt by remember { mutableStateOf("150.00") }
    var plate by remember { mutableStateOf("BRA2E19") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .border(1.dp, HighDensityOutline, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            color = HighDensitySurfaceCard
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Novo Pedido Pago (Simulado)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = HighDensityTextMain
                )
                Text(
                    text = "Isto insere um pedido pago na interface web (como se o cliente tivesse pago pelo celular), gerando o comando Supabase para o aplicativo.",
                    fontSize = 11.sp,
                    color = HighDensityTextSecondary
                )

                OutlinedTextField(
                    value = pump,
                    onValueChange = { pump = it },
                    label = { Text("Número da Bomba") },
                    modifier = Modifier.fillMaxWidth().testTag("sim_pump"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = HighDensityTextMain,
                        unfocusedTextColor = HighDensityTextSecondary,
                        focusedBorderColor = HighDensityPrimary,
                        unfocusedBorderColor = HighDensityOutline,
                        focusedLabelColor = HighDensityPrimary,
                        unfocusedLabelColor = HighDensityTextSecondary
                    )
                )

                OutlinedTextField(
                    value = fuel,
                    onValueChange = { fuel = it },
                    label = { Text("Tipo de Combustível") },
                    modifier = Modifier.fillMaxWidth().testTag("sim_fuel"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = HighDensityTextMain,
                        unfocusedTextColor = HighDensityTextSecondary,
                        focusedBorderColor = HighDensityPrimary,
                        unfocusedBorderColor = HighDensityOutline,
                        focusedLabelColor = HighDensityPrimary,
                        unfocusedLabelColor = HighDensityTextSecondary
                    )
                )

                OutlinedTextField(
                    value = valueTxt,
                    onValueChange = { valueTxt = it },
                    label = { Text("Valor Total (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("sim_value"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = HighDensityTextMain,
                        unfocusedTextColor = HighDensityTextSecondary,
                        focusedBorderColor = HighDensityPrimary,
                        unfocusedBorderColor = HighDensityOutline,
                        focusedLabelColor = HighDensityPrimary,
                        unfocusedLabelColor = HighDensityTextSecondary
                    )
                )

                OutlinedTextField(
                    value = plate,
                    onValueChange = { plate = it },
                    label = { Text("Placa do Veículo (Opcional)") },
                    modifier = Modifier.fillMaxWidth().testTag("sim_plate"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = HighDensityTextMain,
                        unfocusedTextColor = HighDensityTextSecondary,
                        focusedBorderColor = HighDensityPrimary,
                        unfocusedBorderColor = HighDensityOutline,
                        focusedLabelColor = HighDensityPrimary,
                        unfocusedLabelColor = HighDensityTextSecondary
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = HighDensityDoneBg),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("sim_cancel_btn")
                    ) {
                        Text("CANCELAR", color = HighDensityTextSecondary, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            val v = valueTxt.toDoubleOrNull() ?: 100.00
                            onSimulate(pump, fuel, v, plate)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("sim_add_btn")
                    ) {
                        Text("SIMULAR ENTRADA", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
