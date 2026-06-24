package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MecanicoViewModel
import com.example.ui.Screen
import com.example.data.Vehicle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: MecanicoViewModel) {
    val language by viewModel.activeLanguage.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val workshop by viewModel.userWorkshop.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val vehicles by viewModel.vehicles.collectAsState()
    val contributions by viewModel.allContributions.collectAsState()
    val unreadNotifs by viewModel.unreadNotificationsCount.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    var showNotifDialog by remember { mutableStateOf(false) }
    var showAddContributionDialog by remember { mutableStateOf(false) }

    // Translations
    val titleHello = if (language == "PT-BR") "Olá," else "Hello,"
    val labelSearch = if (language == "PT-BR") "Buscar modelo, código ou defeito..." else "Search model, part code or defect..."
    val labelCategories = if (language == "PT-BR") "Categorias" else "Categories"
    val catCar = if (language == "PT-BR") "Carros" else "Cars"
    val catMoto = if (language == "PT-BR") "Motos" else "Bikes"
    val catTruck = if (language == "PT-BR") "Caminhões" else "Trucks"
    val titlePopular = if (language == "PT-BR") "Modelos Populares" else "Popular Models"
    val titleContributions = if (language == "PT-BR") "Dicas da Comunidade" else "Community Tips"
    val btnAddTip = if (language == "PT-BR") "Adicionar Dica" else "Add Tip"
    val labelNoVehicles = if (language == "PT-BR") "Nenhum veículo encontrado" else "No vehicles found"
    val labelRecentTips = if (language == "PT-BR") "Compartilhe seu conhecimento técnico" else "Share your technical knowledge"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "$titleHello $userName",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = workshop,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(
                            onClick = {
                                showNotifDialog = true
                                viewModel.markAllNotificationsRead()
                            },
                            modifier = Modifier.testTag("notification_bell")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (unreadNotifs > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-4).dp, y = 4.dp)
                                    .size(18.dp)
                                    .background(MaterialTheme.colorScheme.error, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = unreadNotifs.toString(),
                                    color = MaterialTheme.colorScheme.onError,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddContributionDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_contribution_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = btnAddTip)
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Quick Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = { Text(labelSearch) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_search_bar"),
                    shape = RoundedCornerShape(28.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF131C2D), // Deep Navy
                        unfocusedContainerColor = Color(0xFF131C2D),
                        disabledContainerColor = Color(0xFF131C2D),
                        focusedBorderColor = Color(0xFF1E2D4A), // Muted border
                        unfocusedBorderColor = Color(0xFF1E2D4A),
                        focusedTextColor = Color(0xFFF1F5F9), // Silver white
                        unfocusedTextColor = Color(0xFFF1F5F9),
                        focusedPlaceholderColor = Color(0xFF94A3B8), // Muted slate gray
                        unfocusedPlaceholderColor = Color(0xFF94A3B8),
                        focusedLeadingIconColor = Color(0xFF94A3B8),
                        unfocusedLeadingIconColor = Color(0xFF94A3B8)
                    )
                )
            }

            // Platform Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Engineering,
                            contentDescription = "AutoPedia",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Column {
                            Text(
                                text = "AutoPedia",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "A única plataforma brasileira que reúne mecânica, elétrica e pintura para carro, moto e caminhão em um só lugar. Conhecimento que move!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Category Quick Buttons
            item {
                Text(
                    text = labelCategories,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CategoryCard(
                        title = catCar,
                        icon = Icons.Default.DirectionsCar,
                        isSelected = viewModel.filterVehicleType.value == "Carro",
                        modifier = Modifier.weight(1f)
                    ) {
                        viewModel.filterVehicleType.value = "Carro"
                        viewModel.changeScreen(Screen.MANUALS)
                    }
                    CategoryCard(
                        title = catMoto,
                        icon = Icons.Default.TwoWheeler,
                        isSelected = viewModel.filterVehicleType.value == "Moto",
                        modifier = Modifier.weight(1f)
                    ) {
                        viewModel.filterVehicleType.value = "Moto"
                        viewModel.changeScreen(Screen.MANUALS)
                    }
                    CategoryCard(
                        title = catTruck,
                        icon = Icons.Default.LocalShipping,
                        isSelected = viewModel.filterVehicleType.value == "Caminhão",
                        modifier = Modifier.weight(1f)
                    ) {
                        viewModel.filterVehicleType.value = "Caminhão"
                        viewModel.changeScreen(Screen.MANUALS)
                    }
                }
            }

            // Popular Models Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = titlePopular,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(
                        onClick = {
                            viewModel.filterVehicleType.value = ""
                            viewModel.changeScreen(Screen.MANUALS)
                        }
                    ) {
                        Text(
                            text = if (language == "PT-BR") "Ver Todos" else "View All",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (vehicles.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(labelNoVehicles, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(vehicles) { vehicle ->
                            PopularVehicleCard(vehicle = vehicle) {
                                viewModel.selectedVehicle.value = vehicle
                                viewModel.changeScreen(Screen.MANUALS)
                            }
                        }
                    }
                }
            }

            // Symptom Quick Diagnosis Tool
            item {
                SymptomDiagnosisCard(
                    viewModel = viewModel,
                    onNavigateToManuals = {
                        viewModel.changeScreen(Screen.MANUALS)
                    },
                    onNavigateToForum = { query ->
                        viewModel.searchQuery.value = query
                        viewModel.changeScreen(Screen.FORUM)
                    }
                )
            }

            // User Contributions Log
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = titleContributions,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = labelRecentTips,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(contributions) { tip ->
                ContributionItemCard(contribution = tip)
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Notifications Dialog
    if (showNotifDialog) {
        AlertDialog(
            onDismissRequest = { showNotifDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(text = if (language == "PT-BR") "Notificações" else "Notifications")
                }
            },
            text = {
                Box(modifier = Modifier.heightIn(max = 400.dp)) {
                    if (notifications.isEmpty()) {
                        Text(text = if (language == "PT-BR") "Nenhuma notificação recente" else "No recent notifications")
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(notifications) { notif ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (notif.isRead) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(12.dp)
                                ) {
                                    Text(text = notif.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = notif.body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotifDialog = false }) {
                    Text("OK", color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }

    // Add Contribution Dialog
    if (showAddContributionDialog) {
        var title by remember { mutableStateOf("") }
        var body by remember { mutableStateOf("") }
        var error by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddContributionDialog = false },
            title = { Text(text = if (language == "PT-BR") "Compartilhar Conhecimento" else "Share Technical Tip") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (language == "PT-BR") "Sua dica ficará visível para toda a comunidade de mecânicos." else "Your tip will be visible to the entire mechanics community.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(if (language == "PT-BR") "Título (Ex: Sensor TPS Gol G4)" else "Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = body,
                        onValueChange = { body = it },
                        label = { Text(if (language == "PT-BR") "Explicação Técnica / Sintomas / Solução" else "Technical Tip / Diagnosis") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        maxLines = 5
                    )
                    if (error.isNotEmpty()) {
                        Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddContributionDialog = false }) {
                    Text(if (language == "PT-BR") "Cancelar" else "Cancel")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isBlank() || body.isBlank()) {
                            error = if (language == "PT-BR") "Preencha todos os campos" else "Fill all fields"
                        } else {
                            viewModel.addUserContribution(viewModel.selectedVehicle.value?.id, title, body)
                            showAddContributionDialog = false
                        }
                    }
                ) {
                    Text(if (language == "PT-BR") "Publicar" else "Post")
                }
            }
        )
    }
}

@Composable
fun CategoryCard(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(96.dp)
            .clickable(onClick = onClick)
            .then(
                if (!isSelected) Modifier.border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(16.dp))
                else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFE53935) else Color(0xFF131C2D) // Brand Red vs Deep Navy
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) Color(0xFFFFFFFF) else Color(0xFFE2E8F0),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color(0xFFFFFFFF) else Color(0xFFE2E8F0)
            )
        }
    }
}

@Composable
fun PopularVehicleCard(
    vehicle: Vehicle,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(110.dp)
            .clickable(onClick = onClick)
            .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF131C2D)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Type Badge
                val icon = when (vehicle.type) {
                    "Moto" -> Icons.Default.TwoWheeler
                    "Caminhão" -> Icons.Default.LocalShipping
                    else -> Icons.Default.DirectionsCar
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFFE53935), // Brand Red Accent
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = vehicle.year.toString(),
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Bold
                )
            }

            Column {
                Text(
                    text = vehicle.model,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF1F5F9),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = vehicle.brand,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

@Composable
fun ContributionItemCard(contribution: com.example.data.UserContribution) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131C2D)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF1A2A44), CircleShape), // Brand Deep Navy background
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = contribution.authorName.take(2).uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8) // Light Sky Blue
                        )
                    }
                    Column {
                        Text(text = contribution.authorName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFF1F5F9))
                        Text(
                            text = "Mecânico Colaborador",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Icon(Icons.Default.Verified, contentDescription = "Verified", tint = Color(0xFFE53935), modifier = Modifier.size(16.dp)) // Brand Red
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(36.dp)
                        .background(Color(0xFFE53935), RoundedCornerShape(2.dp)) // Brand Red
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = contribution.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFFE53935))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "\"${contribution.body}\"",
                        fontSize = 13.sp,
                        color = Color(0xFFE2E8F0),
                        style = MaterialTheme.typography.bodyMedium.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF3B1214), RoundedCornerShape(4.dp)) // Deep Red Burgundy
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("DICA", fontSize = 10.sp, color = Color(0xFFFFA4A2), fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .background(Color(0xFF1A2A44), RoundedCornerShape(4.dp)) // Deep Blue Container
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("COLABORAÇÃO", fontSize = 10.sp, color = Color(0xFFE2E8F0))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomDiagnosisCard(
    viewModel: MecanicoViewModel,
    onNavigateToManuals: () -> Unit,
    onNavigateToForum: (String) -> Unit
) {
    val language by viewModel.activeLanguage.collectAsState()
    val vehicles by viewModel.vehicles.collectAsState()

    var selectedVehicleIndex by remember { mutableStateOf(0) }
    val currentVehicle = vehicles.getOrNull(selectedVehicleIndex)

    var symptomQuery by remember { mutableStateOf("") }
    var showResults by remember { mutableStateOf(false) }

    // Predefined symptoms data
    val predefinedSymptoms = listOf(
        // Onix
        SymptomDiagnosisData("Onix", "Falha na ignição / Motor engasgando", listOf("1. Bobina de Ignição trincada (70% freq)", "2. Cabos de vela com fuga de corrente (20% freq)", "3. Combustível adulterado (10% freq)"), "Bobina de Ignição", "Fácil"),
        SymptomDiagnosisData("Onix", "Luz da injeção acesa constante", listOf("1. Sonda Lambda travada (60% freq)", "2. Sensor MAP sujo (25% freq)", "3. Catalisador entupido (15% freq)"), "Sensor de Oxigênio (Sonda Lambda)", "Médio"),
        SymptomDiagnosisData("Onix", "Ar condicionado parou de gelar", listOf("1. Vazamento na válvula de expansão (55% freq)", "2. Fusível queimado do compressor (25% freq)", "3. Falta de gás R134a (20% freq)"), "Módulo de Controle do Ar Condicionado", "Profissional"),
        
        // Strada
        SymptomDiagnosisData("Strada", "Tranco nas arrancadas em 1ª marcha", listOf("1. Coxim inferior do motor quebrado (65% freq)", "2. Desgaste no disco de embreagem (25% freq)", "3. Tulipa e trizeta com folga (10% freq)"), "Coxim do Motor Firefly", "Médio"),
        SymptomDiagnosisData("Strada", "Rangido forte na traseira com peso", listOf("1. Falta de lubrificação seca no feixe de molas (80% freq)", "2. Bucha da suspensão traseira estourada (15% freq)", "3. Amortecedor estourado (5% freq)"), "Feixe de Molas Traseiro", "Fácil"),
        SymptomDiagnosisData("Strada", "Frente desalinhada ou puxando", listOf("1. Pivot da bandeja de suspensão com folga (50% freq)", "2. Buchas da bandeja de suspensão estouradas (30% freq)", "3. Pneu deformado (20% freq)"), "Amortecedor Dianteiro", "Profissional"),

        // Gol
        SymptomDiagnosisData("Gol", "Luz EPC acesa no painel", listOf("1. Interruptor do pedal de freio queimado (60% freq)", "2. Corpo de borboleta (TBI) sujo (30% freq)", "3. Chicote rompido (10% freq)"), "Corpo de Borboleta (TBI)", "Médio"),
        SymptomDiagnosisData("Gol", "Sem força ao acelerar (motor manco)", listOf("1. Cabo de vela rompido / Vela gasta (50% freq)", "2. Sensor de fase EA111 com defeito (35% freq)", "3. Bomba de combustível fraca (15% freq)"), "Sensor de Fase EA111", "Médio"),
        SymptomDiagnosisData("Gol", "Ponteiro de combustível marca errado", listOf("1. Sensor de nível de combustível oxidado (85% freq)", "2. Boia do tanque trancada (10% freq)", "3. Curto no chicote do painel (5% freq)"), "Sensor de Nível de Combustível", "Fácil"),

        // CG 160
        SymptomDiagnosisData("CG", "Ruído metálico tec-tec no motor", listOf("1. Corrente de comando frouxa ou tensionador cansado (75% freq)", "2. Válvulas desreguladas com folga excessiva (20% freq)", "3. Falta de óleo lubrificante (5% freq)"), "Tensionador da Corrente de Comando", "Médio"),
        SymptomDiagnosisData("CG", "Moto falha quando esquenta", listOf("1. Cachimbo de vela com fuga de corrente (65% freq)", "2. Bobina de pulso falhando (25% freq)", "3. Bico injetor sujo (10% freq)"), "Eletroinjetor de Combustível", "Médio"),
        SymptomDiagnosisData("CG", "Vela de ignição frouxa ou espanada", listOf("1. Rosca do cabeçote espanada por aperto excessivo (90% freq)"), "Vela de Ignição de Alta Performance", "Profissional"),

        // Constellation / Accelo
        SymptomDiagnosisData("Constellation", "Perda de potência em subidas carregado", listOf("1. Filtro de combustível obstruído (50% freq)", "2. Sensor de pressão do turbo falhando (30% freq)", "3. Turbina com desgaste (20% freq)"), "Filtro de Combustível Primário", "Fácil"),
        SymptomDiagnosisData("Constellation", "Luz de falha do motor / Erro de emissões", listOf("1. Cristalização de ureia na válvula dosadora (70% freq)", "2. Sensor de NoX danificado (20% freq)", "3. Bomba do Arla queimada (10% freq)"), "Sensor de NoX de Escapamento", "Profissional"),
        SymptomDiagnosisData("Constellation", "Arla 32 cristalizado no bico", listOf("1. Uso de ureia não homologada / falta de limpeza periódica (95% freq)"), "Módulo de Bombeamento de Arla 32", "Médio")
    )

    // Filtered chips based on current vehicle
    val activeChips = currentVehicle?.let { vehicle ->
        predefinedSymptoms.filter { it.vehicleModel.lowercase() in vehicle.model.lowercase() || it.vehicleModel.lowercase() in vehicle.brand.lowercase() }
    } ?: emptyList()

    val matchedSymptom = remember(currentVehicle, symptomQuery) {
        val query = symptomQuery
        activeChips.find { 
            it.symptomName.lowercase().contains(query.lowercase()) ||
            query.lowercase().contains(it.symptomName.lowercase()) ||
            (query.length > 3 && it.symptomName.lowercase().split(" ").any { word -> word.length > 3 && query.lowercase().contains(word) })
        } ?: activeChips.firstOrNull() ?: predefinedSymptoms.first { it.vehicleModel == "Onix" }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(16.dp))
            .testTag("symptom_diagnosis_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131C2D)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.QueryStats, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                Text(
                    text = if (language == "PT-BR") "DIAGNÓSTICO RÁPIDO" else "QUICK SYMPTOM DIAGNOSIS",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Descreva o sintoma em linguagem natural para identificar possíveis causas e peças relacionadas.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 1. Vehicle Selector Dropdown
            Text("Selecione o Veículo:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(vehicles.size) { index ->
                    val vehicle = vehicles[index]
                    val isSelected = index == selectedVehicleIndex
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF1E2D4A))
                            .clickable {
                                selectedVehicleIndex = index
                                showResults = false
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = vehicle.model,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color(0xFFF1F5F9)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Preset Symptom Chips
            if (activeChips.isNotEmpty()) {
                Text("Sintomas Comuns Encontrados:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    activeChips.take(2).forEach { symptom ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF1E2D4A))
                                .clickable {
                                    symptomQuery = symptom.symptomName
                                    showResults = true
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(symptom.symptomName.split(" / ").first(), fontSize = 10.sp, color = Color(0xFFF1F5F9))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 3. Natural Language Search
            OutlinedTextField(
                value = symptomQuery,
                onValueChange = {
                    symptomQuery = it
                    showResults = false
                },
                placeholder = { Text("Ex: barulho metálico batendo, fumaça azul...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFFF1F5F9),
                    unfocusedTextColor = Color(0xFFF1F5F9)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { showResults = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Analisar Sintoma", fontSize = 12.sp)
            }

            // 4. Structured Results Card
            AnimatedVisibility(visible = showResults) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .background(Color(0xFF0F1F3D), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("DIAGNÓSTICO SUGESTIVO", fontSize = 10.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        
                        // Difficulty badge
                        val badgeColor = when (matchedSymptom.difficulty) {
                            "Fácil" -> Color(0xFF2E7D32)
                            "Médio" -> Color(0xFFEF6C00)
                            else -> Color(0xFFC62828)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(badgeColor)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Dificuldade: ${matchedSymptom.difficulty}", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Text(
                        text = "Sintoma: ${matchedSymptom.symptomName}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )

                    HorizontalDivider(color = Color(0xFF1E2D4A))

                    Text("POSSÍVEIS CAUSAS (POR FREQUÊNCIA):", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF94A3B8))
                    matchedSymptom.causes.forEach { cause ->
                        Text("• $cause", fontSize = 12.sp, color = Color(0xFFE2E8F0))
                    }

                    HorizontalDivider(color = Color(0xFF1E2D4A))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("PEÇA RELACIONADA:", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF94A3B8))
                            Text(matchedSymptom.relatedPartName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Button(
                            onClick = {
                                if (currentVehicle != null) {
                                    viewModel.selectedVehicle.value = currentVehicle
                                    onNavigateToManuals()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Ver Peça", fontSize = 11.sp)
                        }
                    }

                    HorizontalDivider(color = Color(0xFF1E2D4A))

                    OutlinedButton(
                        onClick = {
                            onNavigateToForum(matchedSymptom.symptomName)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Forum, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ver relatos da comunidade sobre este problema", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

data class SymptomDiagnosisData(
    val vehicleModel: String,
    val symptomName: String,
    val causes: List<String>,
    val relatedPartName: String,
    val difficulty: String
)
