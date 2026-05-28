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
    val isLoggedIn by configViewModel.isLoggedIn.collectAsState()
    val selectedProfile by configViewModel.selectedProfile.collectAsState()
    val userEmail by configViewModel.userEmail.collectAsState()
    val companyId by configViewModel.companyId.collectAsState()

    var currentTab by remember { mutableStateOf("painel") }
    val context = LocalContext.current

    // Active payment trigger state
    var activePaymentOrder by remember { mutableStateOf<FuelOrder?>(null) }
    var showPaymentDialog by remember { mutableStateOf(false) }

    if (!isLoggedIn) {
        LoginScreen(onLogin = { email, pass -> configViewModel.login(email, pass) })
        return
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = HighDensityBg
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Profile & Role Selector (Multi-APK demonstration switcher)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .border(1.dp, HighDensityOutline, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = HighDensitySurfaceCard)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "🛠️ MULTI-APK • MUDAR PERFIL INTERATIVO",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = HighDensityPrimary,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(
                            Triple("frentista", "Maquininha Frentista", Icons.Default.Contactless),
                            Triple("caixa", "Dashboard Caixa", Icons.Default.Dashboard),
                            Triple("cliente", "App Cliente Pay", Icons.Default.Smartphone)
                        ).forEach { (profileId, label, icon) ->
                            val isSelected = selectedProfile == profileId
                            Button(
                                onClick = { configViewModel.updateSelectedProfile(profileId) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) HighDensityPrimary else HighDensityDoneBg
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp),
                                    tint = if (isSelected) Color.White else HighDensityTextSecondary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSelected) Color.White else HighDensityTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Body rendering based on current profile simulation
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedProfile) {
                    "frentista" -> {
                        Scaffold(
                            bottomBar = {
                                NavigationBar(
                                    containerColor = HighDensitySurfaceCard,
                                    tonalElevation = 8.dp,
                                    modifier = Modifier.height(64.dp)
                                ) {
                                    NavigationBarItem(
                                        selected = currentTab == "painel",
                                        onClick = { currentTab = "painel" },
                                        icon = { Icon(Icons.Default.LocalGasStation, contentDescription = "Painel") },
                                        label = { Text("Painel", fontSize = 11.sp) },
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
                                        label = { Text("Ajustes", fontSize = 11.sp) },
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
                            }
                        }
                    }
                    "caixa" -> {
                        CaixaScreen(
                            viewModel = painelViewModel,
                            configViewModel = configViewModel
                        )
                    }
                    "cliente" -> {
                        ClienteScreen(
                            viewModel = painelViewModel,
                            configViewModel = configViewModel
                        )
                    }
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
                        onRemoveClick = { viewModel.removeOrder(order.id) },
                        onConfirmFueling = { viewModel.executeActionConcluir(order.id) }
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
    onRemoveClick: () -> Unit,
    onConfirmFueling: () -> Unit
) {
    val isFromClient = order.status == "paid_client_app"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isFromClient) HighDensitySecondary else HighDensityOutline,
                RoundedCornerShape(16.dp)
            ),
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
                            .background(if (isFromClient) HighDensityGreenBadgeBg else HighDensityDoneBg)
                            .border(1.dp, if (isFromClient) HighDensitySecondary else HighDensityOutline, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Text(
                                text = "BOMBA",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isFromClient) HighDensityGreenBadgeText else HighDensityTextSecondary
                            )
                            Text(
                                text = order.pump_number,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isFromClient) HighDensitySecondary else HighDensityTextMain
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
                        color = if (isFromClient) HighDensitySecondary else HighDensityPrimary
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

            if (isFromClient) {
                // Special Banner indicating online payment complete
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .border(1.dp, HighDensitySecondary.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = HighDensityGreenBadgeBg.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = HighDensitySecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "PAGAMENTO DETECTADO VIA REALTIME 📱",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                color = HighDensityGreenBadgeText
                            )
                            Text(
                                text = "Efetuado via App AbastecIA (${order.plugpag_card_last4 ?: "PIX/Saldo"})",
                                fontSize = 10.sp,
                                color = HighDensityTextSecondary
                            )
                        }
                    }
                }
            }

            // Action row inside Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Primary action: Cobrar na Maquininha OR Liberar Despacho
                Button(
                    onClick = {
                        if (isFromClient) {
                            onConfirmFueling()
                        } else {
                            onPaymentClick()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("pay_order_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFromClient) HighDensitySecondary else HighDensityPrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isFromClient) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "IMPRIMIR COMPROVANTE & LIBERAR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    } else {
                        Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "COBRAR NA MAQUININHA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
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
    val companyId by viewModel.companyId.collectAsState()
    val useSimulation by viewModel.useSimulation.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Configuração da Sessão",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = HighDensityTextMain
            )
            Text(
                text = "Associação segura de banco de dados vinculados à credencial do Operador",
                fontSize = 12.sp,
                color = HighDensityTextSecondary
            )
        }

        // Section: Session Operator information
        item {
            Card(
                modifier = Modifier.border(1.dp, HighDensityOutline, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = HighDensitySurfaceCard)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(HighDensityPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = HighDensityPrimary, modifier = Modifier.size(22.dp))
                        }
                        Column {
                            Text("Operador Ativo", fontSize = 10.sp, color = HighDensityTextSecondary, fontWeight = FontWeight.SemiBold)
                            Text(userEmail.ifBlank { "gerente@abastecia.com" }, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = HighDensityTextMain)
                        }
                    }

                    Divider(color = HighDensityOutline)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("ID do Posto Conectado", fontSize = 10.sp, color = HighDensityTextSecondary)
                            Text(companyId, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = HighDensityTextMain)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Banco de Dados", fontSize = 10.sp, color = HighDensityTextSecondary)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(HighDensitySecondary))
                                Text("Online & Protegido", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = HighDensitySecondary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = { viewModel.logout() },
                        colors = ButtonDefaults.buttonColors(containerColor = HighDensityTertiary.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null, tint = HighDensityTertiary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("DESCONECTAR SESSÃO", color = HighDensityTertiary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        // Section: Live sync notes
        item {
            Card(
                modifier = Modifier.border(1.dp, HighDensityOutline, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = HighDensitySurfaceCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Especificações de Segurança do Cliente",
                        fontWeight = FontWeight.Bold,
                        color = HighDensityTextMain,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Para manter as diretrizes de compliance e segurança operacional do posto, as conexões de microsserviços Supabase Realtime são blindadas no servidor backend. Chaves sensíveis de produção não são carregadas ou editadas nesta interface.",
                        fontSize = 11.sp,
                        color = HighDensityTextSecondary,
                        lineHeight = 15.sp
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLogin: (String, String) -> Unit
) {
    var email by remember { mutableStateOf("gerente@abastecia.com") }
    var password by remember { mutableStateOf("••••••••") }
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = HighDensityBg
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Brand Icon ⛽
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(HighDensityPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⛽", fontSize = 32.sp)
                }

                Text(
                    text = "abastecIA",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = HighDensityTextMain
                )

                Text(
                    text = "Sistema de Atendimento Integrado",
                    fontSize = 14.sp,
                    color = HighDensityTextSecondary,
                    textAlign = TextAlign.Center
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, HighDensityOutline, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = HighDensitySurfaceCard)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Login do Operador",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = HighDensityTextMain
                        )

                        Text(
                            text = "Insira suas credenciais corporativas. O login identifica automaticamente o banco de dados e as credenciais de seu posto no backend.",
                            fontSize = 11.sp,
                            color = HighDensityTextSecondary,
                            lineHeight = 15.sp
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("E-mail / Usuário") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth().testTag("login_email"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = HighDensityTextMain,
                                unfocusedTextColor = HighDensityTextSecondary,
                                focusedBorderColor = HighDensityPrimary,
                                unfocusedBorderColor = HighDensityOutline
                            )
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Senha de Acesso") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth().testTag("login_password"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = HighDensityTextMain,
                                unfocusedTextColor = HighDensityTextSecondary,
                                focusedBorderColor = HighDensityPrimary,
                                unfocusedBorderColor = HighDensityOutline
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                if (email.isBlank() || password.isBlank()) {
                                    Toast.makeText(context, "Por favor, preencha todos os campos!", Toast.LENGTH_SHORT).show()
                                } else {
                                    onLogin(email, password)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("login_submit_btn")
                        ) {
                            Text("ENTRAR NO SISTEMA", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                // Quick access templates
                Text(
                    text = "Acesso Rápido para Demonstração:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = HighDensityTextTertiary
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(
                        "gerente@abastecia.com",
                        "operador.sul@abastecia.com"
                    ).forEach { templateEmail ->
                        Button(
                            onClick = {
                                email = templateEmail
                                password = "demo"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HighDensityDoneBg),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f).height(32.dp)
                        ) {
                            Text(
                                text = templateEmail.substringBefore("@"),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = HighDensityTextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaixaScreen(
    viewModel: PainelViewModel,
    configViewModel: ConfigViewModel,
) {
    val companyId by configViewModel.companyId.collectAsState()
    val pendingOrders by viewModel.pendingOrders.collectAsState()
    val completedOrders by viewModel.completedOrders.collectAsState()

    var pumpNum by remember { mutableStateOf("03") }
    var fuelType by remember { mutableStateOf("Gasolina Aditivada") }
    var amountVal by remember { mutableStateOf("150.00") }
    var carPlate by remember { mutableStateOf("BRA2E19") }

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Page Title Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(HighDensityPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Dashboard, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Column {
                    Text("Painel Monitor de Caixas (Dashboard)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = HighDensityTextMain)
                    Text("Posto de combustível ativo: $companyId", fontSize = 11.sp, color = HighDensityTextSecondary)
                }
            }
        }

        // Section: Active pump indicators
        item {
            Text("MONITORAMENTO DE BOMBAS EM TEMPO REAL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = HighDensityPrimary, letterSpacing = 0.5.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    Pair("Bomba 01", "Diesel S-10"),
                    Pair("Bomba 02", "Etanol Comum"),
                    Pair("Bomba 03", "Gasolina Adit"),
                    Pair("Bomba 04", "Gasolina Comum")
                ).forEachIndexed { index, (pumpName, activeFuel) ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, HighDensityOutline, RoundedCornerShape(10.dp)),
                        colors = CardDefaults.cardColors(containerColor = HighDensitySurfaceCard)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(pumpName, fontSize = 11.sp, fontWeight = FontWeight.Black, color = HighDensityTextMain)
                            Text(activeFuel, fontSize = 9.sp, color = HighDensityTextSecondary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (index % 2 == 0) HighDensitySecondary else HighDensityPrimary)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(if (index % 2 == 0) "Pronto" else "Abastecendo", fontSize = 8.sp, color = HighDensityTextTertiary)
                        }
                    }
                }
            }
        }

        // Section: Inject fueling command form
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, HighDensityOutline, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = HighDensitySurfaceCard)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = HighDensityPrimary)
                        Text("Iniciar Venda de Abastecimento (Caixa)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HighDensityTextMain)
                    }

                    Text(
                        "Gere e empurre um abastecimento concluído diretamente do bico de combustível para a fila do Smart POS / frentista.",
                        fontSize = 11.sp,
                        color = HighDensityTextSecondary,
                        lineHeight = 15.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = pumpNum,
                            onValueChange = { pumpNum = it },
                            label = { Text("Bomba", fontSize = 11.sp) },
                            modifier = Modifier.weight(0.4f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = HighDensityTextMain,
                                unfocusedTextColor = HighDensityTextSecondary,
                                focusedBorderColor = HighDensityPrimary,
                                unfocusedBorderColor = HighDensityOutline
                            )
                        )

                        OutlinedTextField(
                            value = amountVal,
                            onValueChange = { amountVal = it },
                            label = { Text("Valor Total (R$)", fontSize = 11.sp) },
                            modifier = Modifier.weight(0.6f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = HighDensityTextMain,
                                unfocusedTextColor = HighDensityTextSecondary,
                                focusedBorderColor = HighDensityPrimary,
                                unfocusedBorderColor = HighDensityOutline
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = fuelType,
                            onValueChange = { fuelType = it },
                            label = { Text("Combustível") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = HighDensityTextMain,
                                unfocusedTextColor = HighDensityTextSecondary,
                                focusedBorderColor = HighDensityPrimary,
                                unfocusedBorderColor = HighDensityOutline
                            )
                        )

                        OutlinedTextField(
                            value = carPlate,
                            onValueChange = { carPlate = it },
                            label = { Text("Placa") },
                            modifier = Modifier.weight(0.8f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = HighDensityTextMain,
                                unfocusedTextColor = HighDensityTextSecondary,
                                focusedBorderColor = HighDensityPrimary,
                                unfocusedBorderColor = HighDensityOutline
                            )
                        )
                    }

                    Button(
                        onClick = {
                            val v = amountVal.toDoubleOrNull() ?: 100.00
                            viewModel.addManualOrder(pumpNum, fuelType, v, carPlate)
                            Toast.makeText(context, "Abastecimento disparado via Realtime!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Icon(Icons.Default.SendToMobile, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("DISPARAR ABASTECIMENTO REALTIME", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // Section: Live transaction monitor
        item {
            Text("VENDAS EM ANDAMENTO NO CAIXA (${pendingOrders.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = HighDensityTextSecondary, letterSpacing = 0.5.sp)
        }

        if (pendingOrders.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = HighDensitySurfaceCard.copy(alpha = 0.5f))
                ) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Sem faturamento pendente no momento", fontSize = 12.sp, color = HighDensityTextTertiary)
                    }
                }
            }
        } else {
            items(pendingOrders) { order ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, HighDensityOutline, RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = HighDensitySurfaceCard)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Bomba ${order.pump_number} • ${order.fuel_type}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = HighDensityTextMain)
                            val detailStatus = if (order.status == "paid_client_app") "CLIENTE PAGOU VIA APP • Aguardando frentista liberar" else "Frentista cobrando na maquininha"
                            Text(detailStatus, fontSize = 10.sp, color = if (order.status == "paid_client_app") HighDensitySecondary else HighDensityTextSecondary)
                        }
                        Text(String.format("R$ %.2f", order.amount), fontWeight = FontWeight.Black, fontSize = 14.sp, color = HighDensityPrimary)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClienteScreen(
    viewModel: PainelViewModel,
    configViewModel: ConfigViewModel,
) {
    val pendingOrders by viewModel.pendingOrders.collectAsState()
    val completedOrders by viewModel.completedOrders.collectAsState()

    // Find the first order that is NOT paid as a candidate for the client to pay
    val orderToPay = pendingOrders.firstOrNull { it.status == "paid" }
    var selectedMethod by remember { mutableStateOf("pix") } // "pix", "saldo"
    var balance by remember { mutableStateOf(450.00) }
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App header mimicking smartphone display card and title
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, HighDensityOutline, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = HighDensitySurfaceCard)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(HighDensityPrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Smartphone, contentDescription = null, tint = HighDensityPrimary, modifier = Modifier.size(16.dp))
                            }
                            Column {
                                Text("Olá, Motorista!", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = HighDensityTextMain)
                                Text("App AbastecIA • Carteira Digital", fontSize = 9.sp, color = HighDensityTextSecondary)
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Seu Saldo", fontSize = 9.sp, color = HighDensityTextSecondary)
                            Text(String.format("R$ %.2f", balance), fontSize = 14.sp, fontWeight = FontWeight.Black, color = HighDensitySecondary)
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "PAGAMENTO DE ABASTECIMENTO DETECTADO 📱",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = HighDensityPrimary,
                letterSpacing = 0.5.sp
            )
        }

        if (orderToPay == null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, HighDensityOutline, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = HighDensitySurfaceCard)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = HighDensityTextTertiary, modifier = Modifier.size(40.dp))
                        Text(
                            text = "Nenhum abastecimento ativo detectado para seu veículo.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = HighDensityTextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "(Para testar, ative o perfil 'Dashboard Caixa' e dispare um abastecimento)",
                            fontSize = 10.sp,
                            color = HighDensityTextTertiary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, HighDensityOutline, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = HighDensitySurfaceCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text("BOMBA ATIVA: ${orderToPay.pump_number}", fontWeight = FontWeight.Black, fontSize = 16.sp, color = HighDensityTextMain)
                                Text(orderToPay.fuel_type, fontSize = 12.sp, color = HighDensityTextSecondary)
                                if (!orderToPay.plate.isNullOrBlank()) {
                                    Text("Placa: ${orderToPay.plate}", fontSize = 11.sp, color = HighDensityTextTertiary, fontFamily = FontFamily.Monospace)
                                }
                            }
                            Text(
                                text = String.format("R$ %.2f", orderToPay.amount),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = HighDensityPrimary
                            )
                        }

                        Divider(color = HighDensityOutline)

                        Text("SELECIONE COMO DESEJA PAGAR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = HighDensityTextSecondary)

                        // Payment Method Selection List
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Method PIX
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedMethod = "pix" }
                                    .border(
                                        1.dp,
                                        if (selectedMethod == "pix") HighDensityPrimary else HighDensityOutline,
                                        RoundedCornerShape(10.dp)
                                    ),
                                colors = CardDefaults.cardColors(containerColor = if (selectedMethod == "pix") HighDensityPrimary.copy(alpha = 0.05f) else Color.Transparent)
                            ) {
                                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.QrCode, contentDescription = null, tint = if (selectedMethod == "pix") HighDensityPrimary else HighDensityTextSecondary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("PIX Online", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = HighDensityTextMain)
                                    Text("Instantâneo", fontSize = 9.sp, color = HighDensityTextSecondary)
                                }
                            }

                            // Method Balance
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedMethod = "saldo" }
                                    .border(
                                        1.dp,
                                        if (selectedMethod == "saldo") HighDensityPrimary else HighDensityOutline,
                                        RoundedCornerShape(10.dp)
                                    ),
                                colors = CardDefaults.cardColors(containerColor = if (selectedMethod == "saldo") HighDensityPrimary.copy(alpha = 0.05f) else Color.Transparent)
                            ) {
                                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = if (selectedMethod == "saldo") HighDensityPrimary else HighDensityTextSecondary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Carteira Saldo", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = HighDensityTextMain)
                                    Text("Débito imediato", fontSize = 9.sp, color = HighDensityTextSecondary)
                                }
                            }
                        }

                        Button(
                            onClick = {
                                if (selectedMethod == "saldo" && balance < orderToPay.amount) {
                                    Toast.makeText(context, "Saldo insuficiente na carteira!", Toast.LENGTH_SHORT).show()
                                } else {
                                    if (selectedMethod == "saldo") {
                                        balance -= orderToPay.amount
                                    }
                                    viewModel.payFromClientApp(orderToPay.id, isPix = (selectedMethod == "pix"))
                                    Toast.makeText(context, "Pagamento Efetuado! Frentista notificado via realtime.", Toast.LENGTH_LONG).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HighDensitySecondary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Icon(Icons.Default.OfflineBolt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CONFIRMAR PAGAMENTO AGORA",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
