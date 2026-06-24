package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.launch
import com.example.ui.MecanicoViewModel
import com.example.ui.Screen
import com.example.ui.theme.SuccessGreen
import com.example.data.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualsScreen(viewModel: MecanicoViewModel) {
    val language by viewModel.activeLanguage.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val vehicles by viewModel.vehicles.collectAsState()
    val selectedVehicle by viewModel.selectedVehicle.collectAsState()

    val typeFilter by viewModel.filterVehicleType.collectAsState()
    val brandFilter by viewModel.filterBrand.collectAsState()
    val yearFilter by viewModel.filterYear.collectAsState()

    var showFilters by remember { mutableStateOf(false) }

    // Translations
    val labelTitle = if (language == "PT-BR") "Manuais Técnicos" else "Technical Manuals"
    val labelSubtitle = if (language == "PT-BR") "Consulte peças, códigos e defeitos crônicos" else "Check parts, codes and chronic defects"
    val labelFilters = if (language == "PT-BR") "Filtros Avançados" else "Advanced Filters"
    val labelVehicleType = if (language == "PT-BR") "Tipo de Veículo" else "Vehicle Type"
    val labelBrand = if (language == "PT-BR") "Fabricante / Marca" else "Manufacturer / Brand"
    val labelYear = if (language == "PT-BR") "Ano do Modelo" else "Model Year"
    val labelClearFilters = if (language == "PT-BR") "Limpar Filtros" else "Clear Filters"
    val labelVehicleFound = if (language == "PT-BR") "veículos encontrados" else "vehicles found"
    val labelSearchEmpty = if (language == "PT-BR") "Nenhum veículo corresponde aos filtros" else "No vehicles match the filters"

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (selectedVehicle == null) labelTitle else selectedVehicle!!.model,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (selectedVehicle != null) {
                            Text(
                                text = "${selectedVehicle!!.brand} • ${selectedVehicle!!.year} • ${selectedVehicle!!.manufacturer}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        } else {
                            Text(
                                text = labelSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (selectedVehicle != null) {
                        IconButton(onClick = { viewModel.selectedVehicle.value = null }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (selectedVehicle == null) {
                        IconButton(
                            onClick = { showFilters = !showFilters },
                            modifier = Modifier.testTag("filters_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (showFilters) Icons.Default.FilterListOff else Icons.Default.FilterList,
                                contentDescription = labelFilters,
                                tint = if (showFilters) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else {
                        // Offline Saved Toggle button!
                        val isSavedFlow = remember(selectedVehicle) {
                            viewModel.isSaved("MANUAL", selectedVehicle!!.id)
                        }
                        val isSaved by isSavedFlow.collectAsState(initial = false)

                        IconButton(
                            onClick = {
                                viewModel.toggleSaveItem(
                                    type = "MANUAL",
                                    refId = selectedVehicle!!.id,
                                    title = selectedVehicle!!.model,
                                    description = "${selectedVehicle!!.brand} (${selectedVehicle!!.year}) - Código e Defeitos Crônicos"
                                )
                            },
                            modifier = Modifier.testTag("offline_save_button")
                        ) {
                            Icon(
                                imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Save Offline",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (selectedVehicle == null) {
                // Advanced Filters Drawer Panel
                AnimatedVisibility(
                    visible = showFilters,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(labelFilters, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                        // 1. Vehicle Type Selectors
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("", "Carro", "Moto", "Caminhão").forEach { type ->
                                val label = if (type.isEmpty()) (if (language == "PT-BR") "Todos" else "All") else type
                                FilterChip(
                                    selected = typeFilter == type,
                                    onClick = { viewModel.filterVehicleType.value = type },
                                    label = { Text(label) }
                                )
                            }
                        }

                        // 2. Brand Selectors
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("", "Chevrolet", "Fiat", "Volkswagen", "Honda", "Mercedes-Benz").forEach { brand ->
                                val label = if (brand.isEmpty()) (if (language == "PT-BR") "Marcas" else "Brands") else brand
                                FilterChip(
                                    selected = brandFilter == brand,
                                    onClick = { viewModel.filterBrand.value = brand },
                                    label = { Text(label) }
                                )
                            }
                        }

                        // 3. Year Filter
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(labelYear, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            listOf(0, 2010, 2013, 2020, 2021, 2022).forEach { year ->
                                val label = if (year == 0) (if (language == "PT-BR") "Todos" else "All") else year.toString()
                                FilterChip(
                                    selected = yearFilter == year,
                                    onClick = { viewModel.filterYear.value = year },
                                    label = { Text(label) }
                                )
                            }
                        }

                        // Clear Button
                        OutlinedButton(
                            onClick = {
                                viewModel.filterVehicleType.value = ""
                                viewModel.filterBrand.value = ""
                                viewModel.filterYear.value = 0
                                viewModel.searchQuery.value = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(labelClearFilters)
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    }
                }

                // Vehicles List view
                if (vehicles.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = labelSearchEmpty,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${vehicles.size} $labelVehicleFound",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(vehicles) { vehicle ->
                            VehicleManualRow(vehicle = vehicle) {
                                viewModel.selectedVehicle.value = vehicle
                            }
                        }
                    }
                }
            } else {
                // Vehicle Manual Details Screen
                VehicleManualDetailsView(vehicle = selectedVehicle!!, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun VehicleManualRow(
    vehicle: Vehicle,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131C2D)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val icon = when (vehicle.type) {
                    "Moto" -> Icons.Default.TwoWheeler
                    "Caminhão" -> Icons.Default.LocalShipping
                    else -> Icons.Default.DirectionsCar
                }
                Icon(
                    imageVector = icon,
                    contentDescription = vehicle.type,
                    tint = Color(0xFFE53935), // Brand Red
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Color(0xFF1A2A44), // Deep Steel Navy
                            RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp)
                )

                Column {
                    Text(
                        text = vehicle.model,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFFF1F5F9)
                    )
                    Text(
                        text = "${vehicle.brand} • ${vehicle.year}",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Details",
                tint = Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
fun VehicleManualDetailsView(
    vehicle: Vehicle,
    viewModel: MecanicoViewModel
) {
    val language by viewModel.activeLanguage.collectAsState()
    val partsFlow = remember(vehicle) { viewModel.getPartsForVehicle(vehicle.id) }
    val parts by partsFlow.collectAsState(initial = emptyList())

    val labelPartsHeader = if (language == "PT-BR") "Peças & Diagnósticos Disponíveis" else "Available Parts & Diagnostics"
    val labelAddNote = if (language == "PT-BR") "Anotações do Mecânico" else "Mechanic Annotations"
    val labelNoParts = if (language == "PT-BR") "Nenhuma peça cadastrada para este veículo" else "No parts registered for this vehicle"

    var expandedPartId by remember { mutableStateOf<Int?>(null) }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var activeTab by remember { mutableStateOf(0) } // 0 = Peças, 1 = Diagrama Explodido, 2 = Wiki Colaborativa

    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        
        // Ficha Técnica Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (language == "PT-BR") "FICHA TÉCNICA" else "SPECIFICATIONS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Modelo: ${vehicle.model}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "Marca: ${vehicle.brand}", fontSize = 13.sp)
                    Text(text = "Montadora: ${vehicle.manufacturer}", fontSize = 13.sp)
                    Text(text = "Ano: ${vehicle.year}", fontSize = 13.sp)
                }
                Button(
                    onClick = { showAddNoteDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.EditNote, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (language == "PT-BR") "Escrever Nota" else "Write Note", fontSize = 12.sp)
                }
            }
        }

        // Material 3 Tab Row
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = Color(0xFF131C2D),
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("Peças", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("Diagrama Explodido", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
            Tab(
                selected = activeTab == 2,
                onClick = { activeTab = 2 },
                text = { Text("Wiki Colaborativa", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
        }

        // Render Active Tab Content
        when (activeTab) {
            0 -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = labelPartsHeader,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (parts.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(labelNoParts, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        }
                    } else {
                        items(parts) { part ->
                            PartAndDefectRow(
                                part = part,
                                isExpanded = expandedPartId == part.id,
                                viewModel = viewModel,
                                onToggleExpand = {
                                    expandedPartId = if (expandedPartId == part.id) null else part.id
                                },
                                onCopyCode = { code ->
                                    clipboard.setText(AnnotatedString(code))
                                    Toast.makeText(context, if (language == "PT-BR") "Código copiado: $code" else "Code copied: $code", Toast.LENGTH_SHORT).show()
                                },
                                onShareWhatsApp = {
                                    val text = """
                                        *AutoPedia - Ficha Técnica de Peça*
                                        *Veículo:* ${vehicle.brand} ${vehicle.model} (${vehicle.year})
                                        *Peça:* ${part.name}
                                        *Código:* ${part.code}
                                        *N/S:* ${part.serialNumber}
                                        
                                        *Problema Crônico Conhecido:*
                                        ${part.chronicProblems}
                                    """.trimIndent()
                                    val sendIntent: Intent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, text)
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, "Compartilhar Peça")
                                    context.startActivity(shareIntent)
                                }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
            1 -> {
                Box(modifier = Modifier.weight(1f)) {
                    ExplodedDiagramComponent(
                        vehicle = vehicle,
                        viewModel = viewModel,
                        onPartSelected = { partId ->
                            activeTab = 0
                            expandedPartId = partId
                        }
                    )
                }
            }
            2 -> {
                Box(modifier = Modifier.weight(1f)) {
                    WikiCollaborativeComponent(
                        vehicle = vehicle,
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    // Add Custom Note Dialog
    if (showAddNoteDialog) {
        var noteTitle by remember { mutableStateOf("") }
        var noteBody by remember { mutableStateOf("") }
        var noteError by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            title = { Text(if (language == "PT-BR") "Adicionar Nota Técnica" else "Add Technical Note") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Escreva uma observação para este veículo (${vehicle.model}). Suas notas nos ajudam a enriquecer os manuais.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    OutlinedTextField(
                        value = noteTitle,
                        onValueChange = { noteTitle = it },
                        label = { Text(if (language == "PT-BR") "Título da Nota (Ex: Ponto da correia)" else "Note Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = noteBody,
                        onValueChange = { noteBody = it },
                        label = { Text(if (language == "PT-BR") "Observação Técnica / Dica de Oficina" else "Technical Tip") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    )
                    if (noteError.isNotEmpty()) {
                        Text(noteError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNoteDialog = false }) {
                    Text(if (language == "PT-BR") "Cancelar" else "Cancel")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noteTitle.isBlank() || noteBody.isBlank()) {
                            noteError = if (language == "PT-BR") "Preencha todos os campos" else "Fill all fields"
                        } else {
                            viewModel.addUserContribution(vehicle.id, noteTitle, noteBody)
                            showAddNoteDialog = false
                            Toast.makeText(context, if (language == "PT-BR") "Nota salva!" else "Note saved!", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text(if (language == "PT-BR") "Salvar" else "Save")
                }
            }
        )
    }
}

@Composable
fun ExplodedDiagramComponent(
    vehicle: Vehicle,
    viewModel: MecanicoViewModel,
    onPartSelected: (Int) -> Unit
) {
    var selectedSystem by remember { mutableStateOf("Motor") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedHotspot by remember { mutableStateOf<Int?>(null) }

    val systems = listOf("Motor", "Suspensão", "Elétrica", "Pintura & Carroceria")

    // Mapped parts for simulation
    val hotspotParts = remember(vehicle, selectedSystem) {
        when (selectedSystem) {
            "Motor" -> listOf(
                DiagramHotspot(1, "Bobina de Ignição", "GM-12642655", "SN-ONX-9011", 50, 45, "Ficha técnica indica bobinas trincadas."),
                DiagramHotspot(2, "Filtro de Óleo", "GM-19347470", "SN-ONX-4422", 120, 160, "Elemento filtrante de papel reforçado."),
                DiagramHotspot(3, "Bico Injetor Keihin", "KH-30100-KVS", "SN-CG-8822", 210, 80, "Alta vazão para motores flex.")
            )
            "Suspensão" -> listOf(
                DiagramHotspot(4, "Amortecedor Dianteiro", "GM-95213141", "SN-ONX-1011", 70, 95, "Vazamento prematuro de fluido hidráulico."),
                DiagramHotspot(5, "Feixe de Molas Traseiro", "FI-4677221", "SN-STR-5544", 220, 180, "Rangido característico por falta de grafite.")
            )
            "Elétrica" -> listOf(
                DiagramHotspot(6, "Placa Cerâmica (Sensor Nível)", "FI-51822055", "SN-STR-8811", 80, 130, "Oxidação química na pista resistiva."),
                DiagramHotspot(7, "Sensor de Oxigênio (Sonda Lambda)", "GM-96942944", "SN-ONX-0033", 190, 100, "Travamento de sinal em circuito fechado.")
            )
            else -> listOf(
                DiagramHotspot(8, "Parachoque Traseiro", "GM-95431221", "SN-ONX-5511", 60, 120, "Tratamento com promotor de aderência plástico."),
                DiagramHotspot(9, "Retrovisor Elétrico Direito", "GM-95241253", "SN-ONX-9933", 220, 130, "Ajuste simétrico das colunas de fixação.")
            )
        }
    }

    val filteredHotspots = hotspotParts.filter {
        it.name.lowercase().contains(searchQuery.lowercase()) ||
        it.oemCode.lowercase().contains(searchQuery.lowercase())
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // System Selector
        ScrollableTabRow(
            selectedTabIndex = systems.indexOf(selectedSystem).coerceAtLeast(0),
            containerColor = Color(0xFF131C2D),
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
        ) {
            systems.forEach { sys ->
                Tab(
                    selected = selectedSystem == sys,
                    onClick = {
                        selectedSystem = sys
                        selectedHotspot = null
                    },
                    text = { Text(sys, fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                )
            }
        }

        // Inline Search in Diagram
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Buscar peça no diagrama...", fontSize = 11.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color(0xFFF1F5F9),
                unfocusedTextColor = Color(0xFFF1F5F9)
            )
        )

        // SVG/Visual Schematic Box (Compose Custom Drawing + Hotspots)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0F1F3D))
                .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(12.dp))
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val gridStep = 40.dp.toPx()
                var x = 0f
                while (x < size.width) {
                    drawLine(
                        color = Color(0xFF1E2D4A).copy(alpha = 0.4f),
                        start = androidx.compose.ui.geometry.Offset(x, 0f),
                        end = androidx.compose.ui.geometry.Offset(x, size.height),
                        strokeWidth = 1f
                    )
                    x += gridStep
                }
                var y = 0f
                while (y < size.height) {
                    drawLine(
                        color = Color(0xFF1E2D4A).copy(alpha = 0.4f),
                        start = androidx.compose.ui.geometry.Offset(0f, y),
                        end = androidx.compose.ui.geometry.Offset(size.width, y),
                        strokeWidth = 1f
                    )
                    y += gridStep
                }
            }

            Icon(
                imageVector = Icons.Default.Build,
                contentDescription = null,
                tint = Color(0xFF1E2D4A).copy(alpha = 0.6f),
                modifier = Modifier
                    .size(140.dp)
                    .align(Alignment.Center)
            )

            // Render interactive hotspots
            filteredHotspots.forEach { hotspot ->
                val isSelected = selectedHotspot == hotspot.id
                Box(
                    modifier = Modifier
                        .offset(x = hotspot.x.dp, y = hotspot.y.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color(0xFFC81E2C) else MaterialTheme.colorScheme.primary)
                        .border(2.dp, Color.White, CircleShape)
                        .clickable { selectedHotspot = hotspot.id }
                        .testTag("hotspot_${hotspot.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = hotspot.id.toString(),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }

            Text(
                text = "Esquema Técnico Interativo. Toque nos balões numerados.",
                color = Color(0xFF94A3B8),
                fontSize = 10.sp,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            )
        }

        // Side/Bottom Panel with Selected Hotspot Details
        AnimatedVisibility(visible = selectedHotspot != null) {
            val hotspot = hotspotParts.find { it.id == selectedHotspot }
            if (hotspot != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFC81E2C).copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131C2D)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(Color(0xFFC81E2C), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(hotspot.id.toString(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Text("DETALHES DA PEÇA SELECIONADA", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF94A3B8))
                            }
                            IconButton(onClick = { selectedHotspot = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                            }
                        }

                        Text(hotspot.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("CÓDIGO OEM", fontSize = 9.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                Text(hotspot.oemCode, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("NÚMERO DE SÉRIE", fontSize = 9.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                Text(hotspot.serialNumber, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            }
                        }

                        Text("Problema Comum: ${hotspot.problem}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFE2E8F0))

                        Button(
                            onClick = {
                                onPartSelected(hotspot.id)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC81E2C)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Ver página completa da peça", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WikiCollaborativeComponent(vehicle: Vehicle, viewModel: MecanicoViewModel) {
    val language by viewModel.activeLanguage.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val wikiSectionsMap by viewModel.wikiSections.collectAsState()
    
    val sections = wikiSectionsMap[vehicle.id] ?: emptyList()
    
    var editingSectionId by remember { mutableStateOf<Int?>(null) }
    var editContent by remember { mutableStateOf("") }
    
    var newCommentText by remember { mutableStateOf("") }
    var commentingSectionId by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Manual Colaborativo Wiki",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (userRole == "Profissional" || userRole == "Mecânico de Veículos") Color(0xFFC81E2C) else Color(0xFF1E2D4A))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Perfil: $userRole",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        
        Text(
            text = "Esta é uma enciclopédia colaborativa. Profissionais podem propor edições técnicas e aprovar alterações para garantir a exatidão dos manuais.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sections) { sec ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131C2D))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = sec.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )

                        Text(
                            text = sec.content,
                            fontSize = 13.sp,
                            color = Color(0xFFE2E8F0)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Última edição por ${sec.lastUpdatedBy} há ${sec.lastUpdatedDaysAgo} dias",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8)
                            )
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(onClick = { 
                                    commentingSectionId = if (commentingSectionId == sec.id) null else sec.id
                                }) {
                                    Icon(Icons.Default.Comment, contentDescription = "Comentários", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                }

                                Button(
                                    onClick = {
                                        editingSectionId = sec.id
                                        editContent = sec.content
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Editar", fontSize = 11.sp)
                                }
                            }
                        }

                        AnimatedVisibility(visible = commentingSectionId == sec.id) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                                    .background(Color(0xFF0F1F3D), RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("COMENTÁRIOS E NOTAS DA COMUNIDADE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                
                                sec.comments.forEach { comment ->
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(comment.author, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                                            Text(comment.timestamp, fontSize = 9.sp, color = Color(0xFF94A3B8))
                                        }
                                        Text(comment.text, fontSize = 12.sp, color = Color(0xFFE2E8F0))
                                        HorizontalDivider(color = Color(0xFF1E2D4A), modifier = Modifier.padding(vertical = 4.dp))
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = newCommentText,
                                        onValueChange = { newCommentText = it },
                                        placeholder = { Text("Adicionar comentário...", fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )
                                    Button(
                                        onClick = {
                                            if (newCommentText.isNotBlank()) {
                                                viewModel.addWikiComment(vehicle.id, sec.id, newCommentText)
                                                newCommentText = ""
                                            }
                                        },
                                        shape = RoundedCornerShape(4.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Text("Enviar", fontSize = 10.sp)
                                    }
                                }
                            }
                        }

                        val pending = sec.pendingProposals.filter { it.status == "PENDING" }
                        if (pending.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                                    .background(Color(0xFF3B1214), RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFFC81E2C), RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("PROPOSTAS DE REVISÃO PENDENTES (${pending.size})", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.White)
                                
                                pending.forEach { prop ->
                                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Autor: ${prop.author}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                            Text(prop.timestamp, fontSize = 10.sp, color = Color(0xFFFFA4A2))
                                        }
                                        Text(text = prop.content, fontSize = 13.sp, color = Color(0xFFF1F5F9))
                                        
                                        if (userRole == "Profissional" || userRole == "Mecânico de Veículos") {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Button(
                                                    onClick = {
                                                        viewModel.approveWikiProposal(vehicle.id, sec.id, prop.id)
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                                    shape = RoundedCornerShape(4.dp),
                                                    contentPadding = PaddingValues(horizontal = 10.dp),
                                                    modifier = Modifier.height(26.dp)
                                                ) {
                                                    Text("Aprovar e Publicar", fontSize = 10.sp)
                                                }
                                                OutlinedButton(
                                                    onClick = {
                                                        viewModel.rejectWikiProposal(vehicle.id, sec.id, prop.id)
                                                    },
                                                    border = BorderStroke(1.dp, Color(0xFFE53935)),
                                                    shape = RoundedCornerShape(4.dp),
                                                    contentPadding = PaddingValues(horizontal = 10.dp),
                                                    modifier = Modifier.height(26.dp)
                                                ) {
                                                    Text("Recusar", fontSize = 10.sp, color = Color(0xFFFFA4A2))
                                                }
                                            }
                                        } else {
                                            Text("Apenas profissionais de nível moderador podem aprovar propostas.", fontSize = 10.sp, color = Color(0xFFFFA4A2))
                                        }
                                        HorizontalDivider(color = Color(0xFFC81E2C).copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 4.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (editingSectionId != null) {
        val activeSec = sections.find { it.id == editingSectionId }
        AlertDialog(
            onDismissRequest = { editingSectionId = null },
            title = { Text("Propor Edição Técnica") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (userRole == "Profissional" || userRole == "Mecânico de Veículos")
                            "Sua proposta será enviada para o painel de aprovação dos mecânicos moderadores da comunidade."
                            else "Apenas usuários com perfil Profissional podem propor edições. Altere seu papel no Perfil para propor!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    if (userRole == "Profissional" || userRole == "Mecânico de Veículos") {
                        OutlinedTextField(
                            value = editContent,
                            onValueChange = { editContent = it },
                            label = { Text("Conteúdo Técnico") },
                            modifier = Modifier.fillMaxWidth().height(150.dp)
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { editingSectionId = null }) {
                    Text("Cancelar")
                }
            },
            confirmButton = {
                if (userRole == "Profissional" || userRole == "Mecânico de Veículos") {
                    Button(
                        onClick = {
                            viewModel.suggestWikiEdit(vehicle.id, editingSectionId!!, editContent)
                            editingSectionId = null
                        }
                    ) {
                        Text("Enviar Proposta")
                    }
                } else {
                    Button(
                        onClick = { editingSectionId = null }
                    ) {
                        Text("OK")
                    }
                }
            }
        )
    }
}

data class DiagramHotspot(
    val id: Int,
    val name: String,
    val oemCode: String,
    val serialNumber: String,
    val x: Int,
    val y: Int,
    val problem: String
)

@Composable
fun PartAndDefectRow(
    part: PartAndDefect,
    isExpanded: Boolean,
    viewModel: MecanicoViewModel,
    onToggleExpand: () -> Unit,
    onCopyCode: (String) -> Unit,
    onShareWhatsApp: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(12.dp))
            .testTag("part_card_${part.id}"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131C2D)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Category Tag
                        val badgeColor = when (part.category) {
                            "Elétrica" -> MaterialTheme.colorScheme.tertiaryContainer
                            "Pintura" -> MaterialTheme.colorScheme.secondaryContainer
                            else -> MaterialTheme.colorScheme.primaryContainer
                        }
                        val textColor = when (part.category) {
                            "Elétrica" -> MaterialTheme.colorScheme.onTertiaryContainer
                            "Pintura" -> MaterialTheme.colorScheme.onSecondaryContainer
                            else -> MaterialTheme.colorScheme.onPrimaryContainer
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(badgeColor)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(part.category, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = textColor)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = part.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Toggle Details",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                    // 1. Part Code & Serial Number
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("CÓDIGO DA PEÇA", fontSize = 10.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            Text(part.code, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("NÚMERO DE SÉRIE (N/S)", fontSize = 10.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            Text(part.serialNumber, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    // 2. Chronic Problems
                    Column {
                        Text("PROBLEMAS CRÔNICOS CONHECIDOS", fontSize = 10.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(part.chronicProblems, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    }

                    // 3. Technical Diagram Outline
                    if (part.diagramUrl.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.GridOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Text("DIAGRAMA TÉCNICO CONCEITUAL", fontSize = 10.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(part.diagramUrl, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }
                    }

                    // 4. Defect Photo / Schema Outline
                    if (part.imageUrl.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Text("REGISTRO FOTOGRÁFICO DE DEFEITO", fontSize = 10.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(part.imageUrl, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }
                    }

                    // --- Regional Pricing & Transparency Section ---
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    
                    Text(
                        text = "TRANSPARÊNCIA DE PREÇOS COOPERATIVA",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )

                    val formatCurrencyLocal = remember {
                        { cents: Long? ->
                            if (cents == null) "R$ 0,00"
                            else java.text.NumberFormat.getCurrencyInstance(java.util.Locale("pt", "BR")).format(cents / 100.0)
                        }
                    }

                    var selectedRegiaoShow by remember { mutableStateOf(RegiaoBrasil.SUDESTE) }
                    val averagesFlow = remember(part) {
                        viewModel.getPrecosMediosForPeca(part.vehicleId ?: 0, part.id)
                    }
                    val regionalAverages by averagesFlow.collectAsState(initial = emptyList())
                    val activeAverage = regionalAverages.find { it.regiao == selectedRegiaoShow }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Estimativa de Custo no Brasil",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Region chip selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            RegiaoBrasil.values().forEach { r ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (selectedRegiaoShow == r) MaterialTheme.colorScheme.primary else Color.Transparent)
                                        .border(1.dp, if (selectedRegiaoShow == r) Color.Transparent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                        .clickable { selectedRegiaoShow = r }
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = r.name.replace("_", " "),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedRegiaoShow == r) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        if (activeAverage == null) {
                            Text(
                                text = "Nenhuma estimativa de preço coletada para esta região ainda.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        } else {
                            val totalMedio = activeAverage.precoMedioPecasCentavos + activeAverage.precoMedioMaoObraCentavos
                            
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Faixa regional:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    Text(
                                        text = "${formatCurrencyLocal(activeAverage.precoMinimoCentavos)} a ${formatCurrencyLocal(activeAverage.precoMaximoCentavos)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Média Peças:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    Text(formatCurrencyLocal(activeAverage.precoMedioPecasCentavos), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Média Mão de Obra:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    Text(formatCurrencyLocal(activeAverage.precoMedioMaoObraCentavos), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Estimativa de Custo Total:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = formatCurrencyLocal(totalMedio),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF10B981)
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Simple horizontal price range meter
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                ) {
                                    val ratio = if (activeAverage.precoMaximoCentavos > activeAverage.precoMinimoCentavos) {
                                        val totalDiff = activeAverage.precoMaximoCentavos - activeAverage.precoMinimoCentavos
                                        val currentDiff = totalMedio - activeAverage.precoMinimoCentavos
                                        (currentDiff.toFloat() / totalDiff.toFloat()).coerceIn(0.1f, 0.9f)
                                    } else {
                                        0.5f
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(ratio)
                                            .background(
                                                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                                    colors = listOf(Color(0xFF34D399), Color(0xFFFBBF24), Color(0xFFF87171))
                                                )
                                            )
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Econômico", fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                    Text("Justo", fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                    Text("Oneroso", fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                }
                            }
                        }
                    }

                    // "Quanto você pagou?" Form Card
                    var showForm by remember { mutableStateOf(false) }
                    
                    if (!showForm) {
                        OutlinedButton(
                            onClick = { showForm = true },
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.AddCard, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Quanto você pagou por este reparo?", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        var valorPecasInput by remember { mutableStateOf("") }
                        var valorMaoObraInput by remember { mutableStateOf("") }
                        var regiaoInput by remember { mutableStateOf(RegiaoBrasil.SUDESTE) }
                        var showSuccessFeedback by remember { mutableStateOf<Boolean?>(null) } // true = verificado, false = moderation, null = none
                        val coroutineScope = rememberCoroutineScope()

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Contribuição de Preços", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                    IconButton(
                                        onClick = { showForm = false },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Fechar", modifier = Modifier.size(16.dp))
                                    }
                                }

                                if (showSuccessFeedback == null) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = valorPecasInput,
                                            onValueChange = { valorPecasInput = it },
                                            label = { Text("Peças (R$)") },
                                            modifier = Modifier.weight(1f).testTag("input_contrib_pecas"),
                                            singleLine = true,
                                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                                        )

                                        OutlinedTextField(
                                            value = valorMaoObraInput,
                                            onValueChange = { valorMaoObraInput = it },
                                            label = { Text("Mão de Obra (R$)") },
                                            modifier = Modifier.weight(1f).testTag("input_contrib_mao_obra"),
                                            singleLine = true,
                                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                                        )
                                    }

                                    // Region selection chip row
                                    Text("Sua Região:", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        RegiaoBrasil.values().forEach { r ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(if (regiaoInput == r) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                                    .border(1.dp, if (regiaoInput == r) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(4.dp))
                                                    .clickable { regiaoInput = r }
                                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                                            ) {
                                                Text(r.name, fontSize = 8.sp, color = if (regiaoInput == r) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
                                            }
                                        }
                                    }

                                    val valP = (valorPecasInput.toDoubleOrNull()?.let { (it * 100).toLong() }) ?: 0L
                                    val valM = (valorMaoObraInput.toDoubleOrNull()?.let { (it * 100).toLong() }) ?: 0L
                                    val totalPaid = valP + valM

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Total Estimado:", fontSize = 11.sp)
                                        Text(formatCurrencyLocal(totalPaid), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }

                                    Button(
                                        onClick = {
                                            if (valP > 0 || valM > 0) {
                                                coroutineScope.launch {
                                                    val verified = viewModel.registrarPrecoReparo(
                                                        veiculoId = part.vehicleId ?: 0,
                                                        pecaId = part.id,
                                                        valorPecasCentavos = valP,
                                                        valorMaoObraCentavos = valM,
                                                        regiao = regiaoInput
                                                    )
                                                    showSuccessFeedback = verified
                                                }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().testTag("submit_price_contrib_btn"),
                                        enabled = valP > 0 || valM > 0
                                    ) {
                                        Text("Enviar Anonimamente", fontSize = 12.sp)
                                    }
                                } else {
                                    // Feedback states
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (showSuccessFeedback == true) Icons.Default.CheckCircle else Icons.Default.Pending,
                                            contentDescription = null,
                                            tint = if (showSuccessFeedback == true) Color(0xFF10B981) else Color(0xFFFBBF24),
                                            modifier = Modifier.size(36.dp)
                                        )
                                        
                                        Text(
                                            text = if (showSuccessFeedback == true) "Contribuição integrada!" else "Contribuição em moderação",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            textAlign = TextAlign.Center
                                        )

                                        Text(
                                            text = if (showSuccessFeedback == true) 
                                                "Sua contribuição foi validada e integrada com sucesso ao banco regional de preços!" 
                                                else "Valores discrepantes detectados em relação à média local. Sua contribuição foi encaminhada para revisão moderada.",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                            textAlign = TextAlign.Center
                                        )

                                        TextButton(onClick = { 
                                            showSuccessFeedback = null
                                            valorPecasInput = ""
                                            valorMaoObraInput = ""
                                            showForm = false
                                        }) {
                                            Text("Fechar")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 5. Actions / External Integration (WhatsApp Share & Code Copy)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onCopyCode(part.code) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copiar Código", fontSize = 12.sp)
                        }

                        Button(
                            onClick = onShareWhatsApp,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SuccessGreen,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("WhatsApp", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
