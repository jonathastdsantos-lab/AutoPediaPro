package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.MecanicoViewModel
import com.example.ui.Screen
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OficinaScreen(viewModel: MecanicoViewModel) {
    val activeScreen by viewModel.activeScreen.collectAsState()

    when (activeScreen) {
        Screen.OFFICE_MAIN -> OficinaMainScreen(viewModel)
        Screen.OFFICE_CREATE -> OficinaCreateScreen(viewModel)
        Screen.OFFICE_DASHBOARD -> OficinaDashboardScreen(viewModel)
        Screen.OFFICE_ADD_VEHICLE -> OficinaAddVehicleScreen(viewModel)
        Screen.OFFICE_VEHICLE_DETAIL -> OficinaVehicleDetailScreen(viewModel)
        Screen.OFFICE_NEW_ATTENDANCE -> OficinaNewAttendanceScreen(viewModel)
        else -> OficinaMainScreen(viewModel)
    }
}

// Helper to format currency
fun formatCurrency(cents: Long?): String {
    if (cents == null) return "R$ 0,00"
    val format = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    return format.format(cents / 100.0)
}

// Helper to format date
fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
    return sdf.format(Date(timestamp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OficinaMainScreen(viewModel: MecanicoViewModel) {
    val userOficinas by viewModel.userOficinas.collectAsState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Minha Oficina / Frota", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.changeScreen(Screen.HOME) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hero card banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1B2C4E) // Steel Blue Accent
                ),
                border = BorderStroke(1.dp, Color(0xFFC81E2C)) // Brand Red
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.BusinessCenter,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color(0xFFC81E2C)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Área de Negócios & Frotas",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Gerencie ordens de serviço, veículos de clientes e mantenha um histórico de manutenção local e integrado.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (userOficinas.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Nenhuma oficina ou frota cadastrada",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Cadastre sua oficina mecânica, auto center, ou departamento de frota empresarial para começar.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.changeScreen(Screen.OFFICE_CREATE) },
                            modifier = Modifier.testTag("create_office_welcome_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Criar Minha Oficina")
                        }
                    }
                }
            } else {
                Text(
                    text = "Suas Oficinas e Frotas",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(userOficinas) { oficina ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectedOficina.value = oficina
                                    viewModel.changeScreen(Screen.OFFICE_DASHBOARD)
                                }
                                .testTag("oficina_card_${oficina.id}"),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            if (oficina.tipo == TipoOficina.OFICINA) Color(0xFF1B2C4E) else Color(
                                                0xFF0F172A
                                            ),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (oficina.tipo == TipoOficina.OFICINA) Icons.Default.HomeRepairService else Icons.Default.LocalShipping,
                                        contentDescription = null,
                                        tint = if (oficina.tipo == TipoOficina.OFICINA) Color(0xFFC81E2C) else Color(
                                            0xFF38BDF8
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = oficina.nome,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${oficina.cidade} - ${oficina.uf} • ${if (oficina.tipo == TipoOficina.OFICINA) "Oficina Mecânica" else "Frota Empresarial"}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    if (!oficina.cnpj.isNullOrEmpty()) {
                                        Text(
                                            text = "CNPJ: ${oficina.cnpj}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.changeScreen(Screen.OFFICE_CREATE) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_new_office_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cadastrar Nova Oficina / Frota")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OficinaCreateScreen(viewModel: MecanicoViewModel) {
    var nome by remember { mutableStateOf("") }
    var cnpj by remember { mutableStateOf("") }
    var uf by remember { mutableStateOf("SP") }
    var cidade by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf(TipoOficina.OFICINA) }

    val ufs = listOf(
        "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA",
        "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN",
        "RS", "RO", "RR", "SC", "SP", "SE", "TO"
    )
    var ufsExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nova Oficina ou Frota", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.changeScreen(Screen.OFFICE_MAIN) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Dados do Estabelecimento",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome da Oficina / Frota *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("office_name_input"),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Store, contentDescription = null) }
                )
            }

            item {
                OutlinedTextField(
                    value = cnpj,
                    onValueChange = { cnpj = it },
                    label = { Text("CNPJ (Opcional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("office_cnpj_input"),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // UF Dropdown
                    Box(modifier = Modifier.width(100.dp)) {
                        OutlinedTextField(
                            value = uf,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("UF *") },
                            trailingIcon = {
                                IconButton(onClick = { ufsExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { ufsExpanded = true }
                        )
                        DropdownMenu(
                            expanded = ufsExpanded,
                            onDismissRequest = { ufsExpanded = false }
                        ) {
                            ufs.forEach { ufStr ->
                                DropdownMenuItem(
                                    text = { Text(ufStr) },
                                    onClick = {
                                        uf = ufStr
                                        ufsExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Cidade Input
                    OutlinedTextField(
                        value = cidade,
                        onValueChange = { cidade = it },
                        label = { Text("Cidade *") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("office_city_input"),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null) }
                    )
                }
            }

            item {
                Text(
                    text = "Tipo de Controle",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilterChip(
                        selected = tipo == TipoOficina.OFICINA,
                        onClick = { tipo = TipoOficina.OFICINA },
                        label = { Text("Oficina Mecânica") },
                        leadingIcon = {
                            if (tipo == TipoOficina.OFICINA) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    )

                    FilterChip(
                        selected = tipo == TipoOficina.FROTA_EMPRESA,
                        onClick = { tipo = TipoOficina.FROTA_EMPRESA },
                        label = { Text("Frota de Empresa") },
                        leadingIcon = {
                            if (tipo == TipoOficina.FROTA_EMPRESA) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    )
                }
            }

            item {
                Button(
                    onClick = {
                        if (nome.isNotBlank() && cidade.isNotBlank()) {
                            viewModel.createOficina(nome, if (cnpj.isBlank()) null else cnpj, uf, cidade, tipo)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .testTag("save_office_btn"),
                    enabled = nome.isNotBlank() && cidade.isNotBlank()
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Salvar Oficina")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OficinaDashboardScreen(viewModel: MecanicoViewModel) {
    val selectedOficina by viewModel.selectedOficina.collectAsState()
    val (searchQuery, setSearchQuery) = remember { mutableStateOf("") }

    if (selectedOficina == null) {
        viewModel.changeScreen(Screen.OFFICE_MAIN)
        return
    }

    val veiculosStream = remember(selectedOficina, searchQuery) {
        viewModel.searchVeiculosAtendidos(selectedOficina!!.id, searchQuery)
    }
    val veiculosAtendidos by veiculosStream.collectAsState(initial = emptyList())

    // Cache vehicles catalog list to map versaoId to vehicle detail
    val catalogVehicles by viewModel.vehicles.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(selectedOficina!!.nome, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("${selectedOficina!!.cidade} - ${selectedOficina!!.uf} • Painel", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.changeScreen(Screen.OFFICE_MAIN) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.changeScreen(Screen.OFFICE_ADD_VEHICLE) },
                containerColor = Color(0xFFC81E2C), // Accent Brand Red
                contentColor = Color.White,
                modifier = Modifier.testTag("add_serviced_vehicle_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Veículo")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = setSearchQuery,
                label = { Text("Buscar placa ou nome do cliente...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("search_serviced_vehicles_input"),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpar")
                        }
                    }
                },
                singleLine = true
            )

            if (veiculosAtendidos.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DriveEta,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Nenhum veículo atendido registrado",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Clique no botão '+' para associar um veículo do catálogo a um cliente e iniciar o prontuário de atendimentos.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Text(
                    text = "Veículos Cadastrados (${veiculosAtendidos.size})",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(veiculosAtendidos) { veiculoAtendido ->
                        val matchedCatalog = catalogVehicles.find { it.id == veiculoAtendido.versaoId }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectedVeiculoAtendido.value = veiculoAtendido
                                    viewModel.changeScreen(Screen.OFFICE_VEHICLE_DETAIL)
                                }
                                .testTag("vehicle_card_${veiculoAtendido.id}"),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = veiculoAtendido.placa ?: "SEM PLACA",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFC81E2C)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF1B2C4E), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = matchedCatalog?.type ?: "Veículo",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${matchedCatalog?.brand ?: ""} ${matchedCatalog?.model ?: ""} (${matchedCatalog?.year ?: veiculoAtendido.anoFabricacao ?: ""})",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                        Text(
                                            text = "Cliente: ${veiculoAtendido.nomeCliente ?: "Não Informado"}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
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
fun OficinaAddVehicleScreen(viewModel: MecanicoViewModel) {
    var placa by remember { mutableStateOf("") }
    var anoFabricacao by remember { mutableStateOf("") }
    var nomeCliente by remember { mutableStateOf("") }
    var contatoCliente by remember { mutableStateOf("") }
    var observacoes by remember { mutableStateOf("") }

    // Selection of vehicle from catalogue
    val catalogVehicles by viewModel.vehicles.collectAsState()
    var selectedCatalogVehicle by remember { mutableStateOf<Vehicle?>(null) }
    var catalogSearchQuery by remember { mutableStateOf("") }
    var showCatalogSelector by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Adicionar Veículo Atendido", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.changeScreen(Screen.OFFICE_DASHBOARD) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Vincular Modelo do Catálogo *",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (selectedCatalogVehicle == null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCatalogSelector = true }
                            .testTag("select_catalog_vehicle_btn"),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Selecionar modelo da enciclopédia", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCatalogSelector = true },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${selectedCatalogVehicle!!.brand} ${selectedCatalogVehicle!!.model}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Ano: ${selectedCatalogVehicle!!.year} • Fabricante: ${selectedCatalogVehicle!!.manufacturer} • Catálogo #${selectedCatalogVehicle!!.id}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                            TextButton(onClick = { showCatalogSelector = true }) {
                                Text("Alterar", color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Informações do Veículo & Cliente",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                OutlinedTextField(
                    value = placa,
                    onValueChange = { placa = it.uppercase() },
                    label = { Text("Placa (ex: ABC1D23)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("serviced_placa_input"),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null) }
                )
            }

            item {
                OutlinedTextField(
                    value = anoFabricacao,
                    onValueChange = { anoFabricacao = it },
                    label = { Text("Ano de Fabricação") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("serviced_ano_input"),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            item {
                OutlinedTextField(
                    value = nomeCliente,
                    onValueChange = { nomeCliente = it },
                    label = { Text("Nome do Cliente") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("serviced_client_name_input"),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                )
            }

            item {
                OutlinedTextField(
                    value = contatoCliente,
                    onValueChange = { contatoCliente = it },
                    label = { Text("Contato do Cliente (ex: WhatsApp)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("serviced_client_contact_input"),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) }
                )
            }

            item {
                OutlinedTextField(
                    value = observacoes,
                    onValueChange = { observacoes = it },
                    label = { Text("Observações (Ex: Riscos na lataria, etc)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("serviced_obs_input"),
                    minLines = 2,
                    maxLines = 4
                )
            }

            item {
                Button(
                    onClick = {
                        val yearInt = anoFabricacao.toIntOrNull()
                        viewModel.addVeiculoAtendido(
                            versaoId = selectedCatalogVehicle!!.id,
                            placa = if (placa.isBlank()) null else placa,
                            anoFabricacao = yearInt,
                            nomeCliente = if (nomeCliente.isBlank()) null else nomeCliente,
                            contatoCliente = if (contatoCliente.isBlank()) null else contatoCliente,
                            observacoes = if (observacoes.isBlank()) null else observacoes
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .testTag("save_serviced_vehicle_btn"),
                    enabled = selectedCatalogVehicle != null
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Salvar Veículo Atendido")
                }
            }
        }

        // Search catalog modal/sheet selector
        if (showCatalogSelector) {
            AlertDialog(
                onDismissRequest = { showCatalogSelector = false },
                title = { Text("Escolher Modelo do Catálogo") },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = catalogSearchQuery,
                            onValueChange = { catalogSearchQuery = it },
                            label = { Text("Buscar modelo...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            singleLine = true
                        )

                        val filteredCatalog = catalogVehicles.filter {
                            catalogSearchQuery.isEmpty() ||
                                    it.model.lowercase().contains(catalogSearchQuery.lowercase()) ||
                                    it.brand.lowercase().contains(catalogSearchQuery.lowercase())
                        }

                        LazyColumn(
                            modifier = Modifier
                                .height(300.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredCatalog) { vehicle ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedCatalogVehicle = vehicle
                                            showCatalogSelector = false
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(text = "${vehicle.brand} ${vehicle.model}", fontWeight = FontWeight.Bold)
                                        Text(text = "Ano: ${vehicle.year} • Tipo: ${vehicle.type}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showCatalogSelector = false }) {
                        Text("Fechar")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OficinaVehicleDetailScreen(viewModel: MecanicoViewModel) {
    val selectedVeiculoAtendido by viewModel.selectedVeiculoAtendido.collectAsState()
    val catalogVehicles by viewModel.vehicles.collectAsState()

    if (selectedVeiculoAtendido == null) {
        viewModel.changeScreen(Screen.OFFICE_DASHBOARD)
        return
    }

    val matchedCatalog = catalogVehicles.find { it.id == selectedVeiculoAtendido!!.versaoId }

    // Fetch chronic problems / parts for this catalog vehicle
    val partsFlow = remember(selectedVeiculoAtendido) {
        viewModel.getPartsForVehicle(selectedVeiculoAtendido!!.versaoId)
    }
    val partsAndDefects by partsFlow.collectAsState(initial = emptyList())

    // Fetch history
    val historicoFlow = remember(selectedVeiculoAtendido) {
        viewModel.getHistoricoForVeiculo(selectedVeiculoAtendido!!.id)
    }
    val historico by historicoFlow.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Prontuário de Atendimento", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.changeScreen(Screen.OFFICE_DASHBOARD) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            Button(
                onClick = { viewModel.changeScreen(Screen.OFFICE_NEW_ATTENDANCE) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFC81E2C)
                ),
                modifier = Modifier
                    .padding(16.dp)
                    .testTag("new_attendance_btn")
            ) {
                Icon(Icons.Default.ReceiptLong, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Novo Atendimento")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Vehicle Identity card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedVeiculoAtendido!!.placa ?: "PLACA NÃO INFORMADA",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC81E2C)
                            )

                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF1B2C4E), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = matchedCatalog?.type ?: "Geral",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "${matchedCatalog?.brand ?: ""} ${matchedCatalog?.model ?: ""} (${matchedCatalog?.year ?: selectedVeiculoAtendido!!.anoFabricacao ?: ""})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("CLIENTE", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                Text(selectedVeiculoAtendido!!.nomeCliente ?: "Não Cadastrado", fontWeight = FontWeight.Medium)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("CONTATO", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                Text(selectedVeiculoAtendido!!.contatoCliente ?: "Não Cadastrado", fontWeight = FontWeight.Medium)
                            }
                        }

                        if (!selectedVeiculoAtendido!!.observacoes.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("OBSERVAÇÕES", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                            Text(selectedVeiculoAtendido!!.observacoes!!, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // Chronic problems card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF0F172A) // Sleek Black/Dark Blue
                    ),
                    border = BorderStroke(1.dp, Color(0xFFC81E2C)) // Brand Red Accent
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFC81E2C))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Defeitos Crônicos Conhecidos",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Atenção a estes itens conhecidos na nossa enciclopédia para este modelo:",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        if (partsAndDefects.isEmpty()) {
                            Text("Nenhum defeito crônico catalogado no momento.", color = Color(0xFF64748B), style = MaterialTheme.typography.bodyMedium)
                        } else {
                            partsAndDefects.forEach { defect ->
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text(
                                        text = defect.name,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFF1F5F9),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = defect.chronicProblems,
                                        color = Color(0xFF94A3B8),
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    HorizontalDivider(color = Color(0xFF1E293B))
                                }
                            }
                        }
                    }
                }
            }

            // Atendimentos History title
            item {
                Text(
                    text = "Histórico de Atendimentos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (historico.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Nenhum histórico registrado para este veículo.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            } else {
                items(historico) { item ->
                    val relatedPart = partsAndDefects.find { it.id == item.problemaId }
                    val itemPartsFlow = remember(item) {
                        viewModel.getPecasForHistorico(item.id)
                    }
                    val itemParts by itemPartsFlow.collectAsState(initial = emptyList())

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = formatDate(item.dataAtendimento),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Text(
                                    text = formatCurrency(item.valorTotalCentavos),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF10B981) // Green accent for values
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            if (item.kmVeiculo != null) {
                                Text(
                                    text = "Quilometragem: ${item.kmVeiculo} km",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = item.descricao,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            if (relatedPart != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFFEF2F2), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Problema Crônico Resolvido: ${relatedPart.name}",
                                        color = Color(0xFFEF4444),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (itemParts.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Peças Substituídas:",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )

                                itemParts.forEach { hp ->
                                    val partDef = partsAndDefects.find { it.id == hp.pecaId }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "• ${partDef?.name ?: "Peça #${hp.pecaId}"} (x${hp.quantidade})",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                        )
                                        Text(
                                            text = formatCurrency(hp.valorUnitarioCentavos),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            if (!item.realizadoPor.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Engineering, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Realizado por: ${item.realizadoPor}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom space for FAB padding
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OficinaNewAttendanceScreen(viewModel: MecanicoViewModel) {
    val selectedVeiculoAtendido by viewModel.selectedVeiculoAtendido.collectAsState()

    if (selectedVeiculoAtendido == null) {
        viewModel.changeScreen(Screen.OFFICE_VEHICLE_DETAIL)
        return
    }

    var descricao by remember { mutableStateOf("") }
    var kmVeiculo by remember { mutableStateOf("") }
    var valorMaoObra by remember { mutableStateOf("") }

    // Dropdown for related chronic problem
    val partsFlow = remember(selectedVeiculoAtendido) {
        viewModel.getPartsForVehicle(selectedVeiculoAtendido!!.versaoId)
    }
    val chronicProblems by partsFlow.collectAsState(initial = emptyList())
    var selectedProblem by remember { mutableStateOf<PartAndDefect?>(null) }
    var showProblemsDropdown by remember { mutableStateOf(false) }

    // Selected replaced parts list
    val selectedParts = remember { mutableStateListOf<Pair<PartAndDefect, Long?>>() } // Pair of Part -> Unit Value (nullable)
    var showPartSelector by remember { mutableStateOf(false) }

    // Calculated total price
    val partsTotal = selectedParts.sumOf { it.second ?: 0L }
    val laborTotal = (valorMaoObra.toDoubleOrNull()?.let { (it * 100).toLong() }) ?: 0L
    val totalSum = partsTotal + laborTotal

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar Novo Atendimento", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.changeScreen(Screen.OFFICE_VEHICLE_DETAIL) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Problema Crônico Relacionado",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Box {
                    OutlinedTextField(
                        value = selectedProblem?.name ?: "Nenhum / Geral / Outro",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showProblemsDropdown = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showProblemsDropdown = true }
                    )

                    DropdownMenu(
                        expanded = showProblemsDropdown,
                        onDismissRequest = { showProblemsDropdown = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        DropdownMenuItem(
                            text = { Text("Geral / Outro problema") },
                            onClick = {
                                selectedProblem = null
                                showProblemsDropdown = false
                            }
                        )
                        chronicProblems.forEach { part ->
                            DropdownMenuItem(
                                text = { Text(part.name) },
                                onClick = {
                                    selectedProblem = part
                                    showProblemsDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Detalhes do Atendimento",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                OutlinedTextField(
                    value = descricao,
                    onValueChange = { descricao = it },
                    label = { Text("Descrição dos serviços prestados *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("attendance_desc_input"),
                    minLines = 3,
                    maxLines = 6
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = kmVeiculo,
                        onValueChange = { kmVeiculo = it },
                        label = { Text("KM Atual") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("attendance_km_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    OutlinedTextField(
                        value = valorMaoObra,
                        onValueChange = { valorMaoObra = it },
                        label = { Text("Mão de Obra (R$)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("attendance_labor_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        leadingIcon = { Text("R$") }
                    )
                }
            }

            // Selected parts section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Peças Trocadas",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    TextButton(onClick = { showPartSelector = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Adicionar Peça")
                    }
                }

                if (selectedParts.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                            Text("Nenhuma peça adicionada a este atendimento.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    }
                } else {
                    selectedParts.forEachIndexed { index, pair ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(pair.first.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Código: ${pair.first.code} • Unitário: ${formatCurrency(pair.second)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                                IconButton(onClick = { selectedParts.removeAt(index) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Deletar", tint = Color(0xFFEF4444))
                                }
                            }
                        }
                    }
                }
            }

            // Receipt Summary card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Resumo de Custos", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Mão de Obra:", color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                            Text(formatCurrency(laborTotal), color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total em Peças:", color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                            Text(formatCurrency(partsTotal), color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Valor Total Geral:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(formatCurrency(totalSum), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        if (descricao.isNotBlank()) {
                            val kmInt = kmVeiculo.toIntOrNull()
                            val partsList = selectedParts.map { Pair(it.first.id, it.second) }
                            viewModel.registrarAtendimento(
                                problemaId = selectedProblem?.id,
                                descricao = descricao,
                                kmVeiculo = kmInt,
                                valorTotalCentavos = totalSum,
                                dataAtendimento = System.currentTimeMillis(),
                                pecas = partsList
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .testTag("save_attendance_btn"),
                    enabled = descricao.isNotBlank()
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Salvar Atendimento")
                }
            }
        }

        // Replaced Part Selector Modal Dialog
        if (showPartSelector) {
            var selectedPartDef by remember { mutableStateOf<PartAndDefect?>(null) }
            var unitPriceStr by remember { mutableStateOf("") }
            var selectorSearch by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showPartSelector = false },
                title = { Text("Escolher Peça") },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (selectedPartDef == null) {
                            OutlinedTextField(
                                value = selectorSearch,
                                onValueChange = { selectorSearch = it },
                                label = { Text("Buscar Peça...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                singleLine = true
                            )

                            val availableParts = chronicProblems.filter {
                                selectorSearch.isEmpty() || it.name.lowercase().contains(selectorSearch.lowercase()) || it.code.lowercase().contains(selectorSearch.lowercase())
                            }

                            LazyColumn(
                                modifier = Modifier
                                    .height(240.dp)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(availableParts) { part ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedPartDef = part },
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(part.name, fontWeight = FontWeight.Bold)
                                            Text("Código: ${part.code} • Categoria: ${part.category}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                                        }
                                    }
                                }
                            }
                        } else {
                            Text("Definir Valor da Peça:", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(selectedPartDef!!.name, style = MaterialTheme.typography.bodyLarge)
                            Text("Código: ${selectedPartDef!!.code}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = unitPriceStr,
                                onValueChange = { unitPriceStr = it },
                                label = { Text("Valor Unitário (R$)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                leadingIcon = { Text("R$") }
                            )
                        }
                    }
                },
                confirmButton = {
                    if (selectedPartDef != null) {
                        Button(onClick = {
                            val rawPrice = unitPriceStr.toDoubleOrNull()?.let { (it * 100).toLong() }
                            selectedParts.add(Pair(selectedPartDef!!, rawPrice))
                            showPartSelector = false
                        }) {
                            Text("Confirmar")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        if (selectedPartDef != null) {
                            selectedPartDef = null
                        } else {
                            showPartSelector = false
                        }
                    }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
