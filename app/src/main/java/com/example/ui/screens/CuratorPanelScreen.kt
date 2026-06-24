package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MecanicoViewModel
import com.example.ui.Screen
import com.example.data.SupabaseExtracaoStaging
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuratorPanelScreen(viewModel: MecanicoViewModel) {
    val context = LocalContext.current
    val language by viewModel.activeLanguage.collectAsState()
    
    val pendingList by viewModel.pendingStagingExtractions.collectAsState()
    val webLogs by viewModel.webLogs.collectAsState()
    val isStagingLoading by viewModel.isStagingLoading.collectAsState()
    val stagingErrorMessage by viewModel.stagingErrorMessage.collectAsState()
    val isEdgeLoading by viewModel.isEdgeFunctionLoading.collectAsState()

    var showSearchDialog by remember { mutableStateOf(false) }
    var editItem by remember { mutableStateOf<SupabaseExtracaoStaging?>(null) }

    // Dialog state for manual search
    var brandInput by remember { mutableStateOf("") }
    var modelInput by remember { mutableStateOf("") }
    var yearInput by remember { mutableStateOf("") }
    var vehicleTypeInput by remember { mutableStateOf("carro") } // carro, moto, caminhao

    // Trigger load when screen opens
    LaunchedEffect(Unit) {
        viewModel.loadStagingData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Painel de Curadoria",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Moderação de Dados Extraídos por IA & Web",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.changeScreen(Screen.PROFILE) },
                        modifier = Modifier.testTag("curator_back_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF050A12))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF050A12))
        ) {
            // Action Bar on Top
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Ações Rápidas de Extração",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFFE2E8F0)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showSearchDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC81E2C)),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("curator_trigger_search_button"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Nova Busca", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                viewModel.buscarListaCompletaLote(
                                    onSuccess = { Toast.makeText(context, it, Toast.LENGTH_LONG).show() },
                                    onError = { Toast.makeText(context, "Erro ao disparar lote: $it", Toast.LENGTH_LONG).show() }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B2C4E)),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("curator_trigger_batch_button"),
                            shape = RoundedCornerShape(8.dp),
                            enabled = !isEdgeLoading
                        ) {
                            Icon(Icons.Default.GridOn, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Busca em Lote", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Info banner if offline or mock simulation is loaded
            stagingErrorMessage?.let { message ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E293B))
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.OfflineBolt,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = message,
                            color = Color(0xFFCBD5E1),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (isStagingLoading || isEdgeLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFFC81E2C))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isEdgeLoading) "Pesquisando e enriquecendo dados..." else "Carregando registros...",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp
                        )
                    }
                }
            } else if (pendingList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color(0xFF1E2D4A).copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircleOutline,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Tudo limpo na curadoria!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Nenhuma extração pendente na fila de revisão. Use os botões acima para disparar novas buscas na web.",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(pendingList) { item ->
                        CurationItemCard(
                            item = item,
                            webLogs = webLogs,
                            onApprove = { viewModel.aprovarStaging(
                                id = item.id,
                                tipoEntidade = item.tipoEntidade,
                                nomeOuPeca = it.nome,
                                sistemaOuDescricao = it.descricao,
                                codigoOem = it.codigoOem,
                                marca = it.marca,
                                modelo = it.modelo,
                                ano = it.ano,
                                categoria = it.sistema
                            )},
                            onReject = { viewModel.rejeitarStaging(item.id) },
                            onEdit = { editItem = item }
                        )
                    }
                }
            }
        }
    }

    // Modal Search dialog
    if (showSearchDialog) {
        AlertDialog(
            onDismissRequest = { showSearchDialog = false },
            containerColor = Color(0xFF0F172A),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(16.dp)),
            title = {
                Text(
                    "Nova Pesquisa Inteligente",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Color.White
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Digite as informações básicas do veículo para disparar uma varredura web com IA.",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )

                    OutlinedTextField(
                        value = brandInput,
                        onValueChange = { brandInput = it },
                        label = { Text("Marca (ex: Chevrolet)", fontSize = 12.sp, color = Color(0xFF94A3B8)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFC81E2C),
                            unfocusedBorderColor = Color(0xFF1E2D4A),
                            focusedLabelColor = Color(0xFFC81E2C),
                            unfocusedLabelColor = Color(0xFF94A3B8),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("curator_search_brand")
                    )

                    OutlinedTextField(
                        value = modelInput,
                        onValueChange = { modelInput = it },
                        label = { Text("Modelo (ex: Onix)", fontSize = 12.sp, color = Color(0xFF94A3B8)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFC81E2C),
                            unfocusedBorderColor = Color(0xFF1E2D4A),
                            focusedLabelColor = Color(0xFFC81E2C),
                            unfocusedLabelColor = Color(0xFF94A3B8),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("curator_search_model")
                    )

                    OutlinedTextField(
                        value = yearInput,
                        onValueChange = { yearInput = it },
                        label = { Text("Ano (ex: 2021)", fontSize = 12.sp, color = Color(0xFF94A3B8)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFC81E2C),
                            unfocusedBorderColor = Color(0xFF1E2D4A),
                            focusedLabelColor = Color(0xFFC81E2C),
                            unfocusedLabelColor = Color(0xFF94A3B8),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("curator_search_year")
                    )

                    // Vehicle Type Choice
                    Text(
                        "Tipo de Veículo:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFFE2E8F0)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("carro", "moto", "caminhao").forEach { type ->
                            FilterChip(
                                selected = vehicleTypeInput == type,
                                onClick = { vehicleTypeInput = type },
                                label = { Text(type.uppercase(), fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFC81E2C),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF1E2D4A),
                                    labelColor = Color(0xFF94A3B8)
                                ),
                                border = null,
                                modifier = Modifier.testTag("curator_search_type_$type")
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val yearNum = yearInput.toIntOrNull() ?: 2020
                        showSearchDialog = false
                        viewModel.dispararBuscaWeb(
                            marca = brandInput,
                            modelo = modelInput,
                            ano = yearNum,
                            tipo = vehicleTypeInput,
                            onSuccess = { Toast.makeText(context, it, Toast.LENGTH_LONG).show() },
                            onError = { Toast.makeText(context, "Erro: $it", Toast.LENGTH_LONG).show() }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC81E2C)),
                    enabled = brandInput.isNotEmpty() && modelInput.isNotEmpty(),
                    modifier = Modifier.testTag("curator_search_dialog_confirm")
                ) {
                    Text("Iniciar Pesquisa", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSearchDialog = false },
                    modifier = Modifier.testTag("curator_search_dialog_cancel")
                ) {
                    Text("Cancelar", color = Color(0xFF94A3B8))
                }
            }
        )
    }

    // Modal Edit before approve Dialog
    editItem?.let { item ->
        val json = remember(item.dadosExtraidos) {
            try { JSONObject(item.dadosExtraidos) } catch (e: Exception) { JSONObject() }
        }

        var editNome by remember { mutableStateOf(json.optString("nome", json.optString("nome_peca", ""))) }
        var editSistema by remember { mutableStateOf(json.optString("sistema", "motor")) }
        var editOem by remember { mutableStateOf(json.optString("codigo_oem", "")) }
        var editMarca by remember { mutableStateOf(json.optString("marca", "")) }
        var editModelo by remember { mutableStateOf(json.optString("modelo", "")) }
        var editAno by remember { mutableStateOf(json.optInt("ano", 2021).toString()) }
        var editDescricao by remember { mutableStateOf(json.optString("descricao", json.optString("detalhes", ""))) }

        AlertDialog(
            onDismissRequest = { editItem = null },
            containerColor = Color(0xFF0F172A),
            modifier = Modifier.border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(16.dp)),
            title = {
                Text(
                    "Editar e Aprovar Registro",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = editNome,
                        onValueChange = { editNome = it },
                        label = { Text("Nome da Peça", fontSize = 11.sp, color = Color(0xFF94A3B8)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFC81E2C), focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("curator_edit_name")
                    )

                    OutlinedTextField(
                        value = editSistema,
                        onValueChange = { editSistema = it },
                        label = { Text("Sistema (ex: motor, eletrica, freios)", fontSize = 11.sp, color = Color(0xFF94A3B8)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFC81E2C), focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("curator_edit_system")
                    )

                    OutlinedTextField(
                        value = editOem,
                        onValueChange = { editOem = it },
                        label = { Text("Código OEM / Part Number", fontSize = 11.sp, color = Color(0xFF94A3B8)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFC81E2C), focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("curator_edit_oem")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = editMarca,
                            onValueChange = { editMarca = it },
                            label = { Text("Marca", fontSize = 11.sp, color = Color(0xFF94A3B8)) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFC81E2C), focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("curator_edit_brand")
                        )
                        OutlinedTextField(
                            value = editModelo,
                            onValueChange = { editModelo = it },
                            label = { Text("Modelo", fontSize = 11.sp, color = Color(0xFF94A3B8)) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFC81E2C), focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            singleLine = true,
                            modifier = Modifier.weight(1.2f).testTag("curator_edit_model")
                        )
                        OutlinedTextField(
                            value = editAno,
                            onValueChange = { editAno = it },
                            label = { Text("Ano", fontSize = 11.sp, color = Color(0xFF94A3B8)) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFC81E2C), focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            singleLine = true,
                            modifier = Modifier.weight(0.8f).testTag("curator_edit_year")
                        )
                    }

                    OutlinedTextField(
                        value = editDescricao,
                        onValueChange = { editDescricao = it },
                        label = { Text("Descrição do Defeito / Detalhes", fontSize = 11.sp, color = Color(0xFF94A3B8)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFC81E2C), focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth().testTag("curator_edit_desc")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val anoVal = editAno.toIntOrNull() ?: 2021
                        viewModel.aprovarStaging(
                            id = item.id,
                            tipoEntidade = item.tipoEntidade,
                            nomeOuPeca = editNome,
                            sistemaOuDescricao = editDescricao,
                            codigoOem = editOem,
                            marca = editMarca,
                            modelo = editModelo,
                            ano = anoVal,
                            categoria = editSistema
                        )
                        editItem = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    modifier = Modifier.testTag("curator_edit_dialog_approve")
                ) {
                    Text("Salvar & Aprovar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { editItem = null },
                    modifier = Modifier.testTag("curator_edit_dialog_cancel")
                ) {
                    Text("Cancelar", color = Color(0xFF94A3B8))
                }
            }
        )
    }
}

@Composable
fun CurationItemCard(
    item: SupabaseExtracaoStaging,
    webLogs: List<com.example.data.SupabaseEnriquecimentoWebLog>,
    onApprove: (ParsedStagingData) -> Unit,
    onReject: () -> Unit,
    onEdit: () -> Unit
) {
    val json = remember(item.dadosExtraidos) {
        try { JSONObject(item.dadosExtraidos) } catch (e: Exception) { JSONObject() }
    }

    // Try multiple keys depending on format
    val nome = json.optString("nome", json.optString("nome_peca", "Peça Indefinida"))
    val sistema = json.optString("sistema", "motor")
    val codigoOem = json.optString("codigo_oem", "N/A")
    val marca = json.optString("marca", "Chevrolet")
    val modelo = json.optString("modelo", "Onix")
    val ano = json.optInt("ano", 2021)
    val descricao = json.optString("descricao", json.optString("detalhes", "Sem descrição adicional."))

    // Match source URL
    val matchingLog = webLogs.find { it.extracaoId == item.id }
    val sourceUrl = matchingLog?.fonteUrl ?: ""

    // Confidence indicator setup
    val conf = item.confianca ?: 0.5
    val confPct = (conf * 100).toInt()
    val confColor = when {
        conf >= 0.8 -> Color(0xFF10B981) // Emerald Green
        conf >= 0.5 -> Color(0xFFF59E0B) // Amber Yellow
        else -> Color(0xFFEF4444) // Coral Red
    }
    val confLabel = when {
        conf >= 0.8 -> "Alta Certeza"
        conf >= 0.5 -> "Certeza Média"
        else -> "Baixa Certeza"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(12.dp))
            .testTag("curation_item_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Entity Badge + Confidence Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Entity Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (item.tipoEntidade == "peca") Color(0xFFC81E2C).copy(alpha = 0.15f)
                            else Color(0xFF3B82F6).copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (item.tipoEntidade == "peca") "PEÇA" else "PROB. CRÔNICO",
                        color = if (item.tipoEntidade == "peca") Color(0xFFEF4444) else Color(0xFF60A5FA),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }

                // Confidence badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(confColor, CircleShape)
                    )
                    Text(
                        text = "$confLabel ($confPct%)",
                        color = confColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main technical title
            Text(
                text = nome,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = Color.White
            )

            // Vehicle application text
            Text(
                text = "Aplicação: $marca $modelo $ano | Sistema: ${sistema.uppercase()}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFC81E2C)
            )

            if (codigoOem.isNotEmpty() && codigoOem != "N/A") {
                Text(
                    text = "Part Number OEM: $codigoOem",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Technical details body
            Text(
                text = descricao,
                fontSize = 13.sp,
                color = Color(0xFFCBD5E1),
                lineHeight = 18.sp
            )

            // Source Log Link URL
            if (sourceUrl.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.clickable { /* action if browser link is wanted, or decorative */ }
                ) {
                    Icon(Icons.Default.Language, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(14.dp))
                    Text(
                        text = "Fonte: $sourceUrl",
                        color = Color(0xFF3B82F6),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFF1E2D4A))
            Spacer(modifier = Modifier.height(12.dp))

            // Button Control Actions (Aprovar, Editar, Rejeitar)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Rejeitar Button
                OutlinedButton(
                    onClick = onReject,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("curator_reject_btn_${item.id}"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Rejeitar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Editar Button
                OutlinedButton(
                    onClick = onEdit,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF59E0B)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f)),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("curator_edit_btn_${item.id}"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Editar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Aprovar Button
                Button(
                    onClick = {
                        onApprove(
                            ParsedStagingData(
                                nome = nome,
                                sistema = sistema,
                                codigoOem = codigoOem,
                                marca = marca,
                                modelo = modelo,
                                ano = ano,
                                descricao = descricao
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    modifier = Modifier
                        .weight(1.1f)
                        .testTag("curator_approve_btn_${item.id}"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Aprovar", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

data class ParsedStagingData(
    val nome: String,
    val sistema: String,
    val codigoOem: String,
    val marca: String,
    val modelo: String,
    val ano: Int,
    val descricao: String
)
