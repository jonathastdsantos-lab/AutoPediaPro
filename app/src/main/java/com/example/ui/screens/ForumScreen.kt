package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MecanicoViewModel
import com.example.data.ForumTopic
import com.example.data.ForumReply

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumScreen(viewModel: MecanicoViewModel) {
    val language by viewModel.activeLanguage.collectAsState()
    val topics by viewModel.forumTopics.collectAsState()
    val selectedTopic by viewModel.selectedTopic.collectAsState()

    var selectedCategoryFilter by remember { mutableStateOf("") } // empty means All
    var showCreateTopicDialog by remember { mutableStateOf(false) }

    // Translations
    val labelForum = if (language == "PT-BR") "Fórum Técnico" else "Technical Forum"
    val labelSubtitle = if (language == "PT-BR") "Tire dúvidas e compartilhe com outros mecânicos" else "Ask questions and share with other mechanics"
    val labelNewTopic = if (language == "PT-BR") "Novo Tópico" else "New Topic"
    val labelMec = if (language == "PT-BR") "Mecânica" else "Mechanics"
    val labelEle = if (language == "PT-BR") "Elétrica" else "Electrical"
    val labelPin = if (language == "PT-BR") "Funilaria/Pintura" else "Bodywork/Paint"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (selectedTopic == null) labelForum else selectedTopic!!.title,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (selectedTopic == null) {
                            Text(
                                text = labelSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        } else {
                            Text(
                                text = "Postado por ${selectedTopic!!.author} • ${selectedTopic!!.category}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (selectedTopic != null) {
                        IconButton(onClick = { viewModel.selectedTopic.value = null }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (selectedTopic != null) {
                        // Follow topic trigger
                        IconButton(
                            onClick = { viewModel.toggleFollowTopic(selectedTopic!!) },
                            modifier = Modifier.testTag("follow_topic_button")
                        ) {
                            Icon(
                                imageVector = if (selectedTopic!!.isFollowed) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                                contentDescription = "Follow Thread",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            if (selectedTopic == null) {
                FloatingActionButton(
                    onClick = { showCreateTopicDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("new_topic_fab")
                ) {
                    Icon(Icons.Default.PostAdd, contentDescription = labelNewTopic)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (selectedTopic == null) {
                // Topic Selector categories header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("", "Mecânica", "Elétrica", "Funilaria/Pintura").forEach { cat ->
                        val label = if (cat.isEmpty()) (if (language == "PT-BR") "Todos" else "All") else cat
                        FilterChip(
                            selected = selectedCategoryFilter == cat,
                            onClick = { selectedCategoryFilter = cat },
                            label = { Text(label, fontSize = 12.sp) }
                        )
                    }
                }

                val filteredTopics = if (selectedCategoryFilter.isEmpty()) topics
                else topics.filter { it.category == selectedCategoryFilter }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredTopics) { topic ->
                        ForumTopicRow(topic = topic) {
                            viewModel.selectedTopic.value = topic
                        }
                    }
                }
            } else {
                // Topic conversation detail view
                ForumTopicDetailView(topic = selectedTopic!!, viewModel = viewModel)
            }
        }
    }

    // Create New Topic Dialog
    if (showCreateTopicDialog) {
        var tTitle by remember { mutableStateOf("") }
        var tBody by remember { mutableStateOf("") }
        var tCategory by remember { mutableStateOf("Mecânica") }
        var tError by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateTopicDialog = false },
            title = { Text(labelNewTopic) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = tTitle,
                        onValueChange = { tTitle = it },
                        label = { Text("Título da Dúvida/Problema") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Simple category pick
                    Text("Selecione a Categoria:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Mecânica", "Elétrica", "Funilaria/Pintura").forEach { cat ->
                            FilterChip(
                                selected = tCategory == cat,
                                onClick = { tCategory = cat },
                                label = { Text(cat, fontSize = 10.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = tBody,
                        onValueChange = { tBody = it },
                        label = { Text("Descreva detalhadamente o sintoma do veículo, testes efetuados ou dicas necessárias...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        maxLines = 5
                    )

                    if (tError.isNotEmpty()) {
                        Text(tError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateTopicDialog = false }) {
                    Text(if (language == "PT-BR") "Cancelar" else "Cancel")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tTitle.isBlank() || tBody.isBlank()) {
                            tError = if (language == "PT-BR") "Preencha todos os campos" else "Fill all fields"
                        } else {
                            viewModel.createForumTopic(tTitle, tBody, tCategory)
                            showCreateTopicDialog = false
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
fun ForumTopicRow(
    topic: ForumTopic,
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
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category badge
                val badgeColor = when (topic.category) {
                    "Elétrica" -> MaterialTheme.colorScheme.tertiaryContainer
                    "Funilaria/Pintura" -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.primaryContainer
                }
                val textColor = when (topic.category) {
                    "Elétrica" -> MaterialTheme.colorScheme.onTertiaryContainer
                    "Funilaria/Pintura" -> MaterialTheme.colorScheme.onSecondaryContainer
                    else -> MaterialTheme.colorScheme.onPrimaryContainer
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(badgeColor)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(topic.category, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = textColor)
                }

                if (topic.isFollowed) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                        Text("Acompanhando", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = topic.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = topic.body, fontSize = 13.sp, maxLines = 2, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Por ${topic.author}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Comment, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Text("Participar", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
fun ForumTopicDetailView(
    topic: ForumTopic,
    viewModel: MecanicoViewModel
) {
    val replies by viewModel.currentTopicReplies.collectAsState()
    var myReplyText by remember { mutableStateOf("") }
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Main Topic Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131C2D)),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TÓPICO PRINCIPAL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Por ${topic.author}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = topic.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = topic.body, fontSize = 14.sp, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Text(
                text = "Respostas dos Mecânicos (${replies.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Replies items
        if (replies.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Ainda não há respostas técnicas. Seja o primeiro a responder!", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        } else {
            items(replies) { reply ->
                ReplyRow(reply = reply)
            }
        }

        // Submit Reply Input Box
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131C2D)),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Escrever uma Resposta Técnica:", fontSize = 11.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = myReplyText,
                        onValueChange = { myReplyText = it },
                        placeholder = { Text("Digite sua solução prática ou teste alternativo...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .testTag("reply_input_field")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (myReplyText.isNotBlank()) {
                                viewModel.addForumReply(topic.id, myReplyText)
                                myReplyText = ""
                                Toast.makeText(context, "Resposta publicada!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.End)
                            .testTag("reply_submit_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Responder")
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ReplyRow(reply: ForumReply) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131C2D)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Engineering, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                    }
                    Text(text = reply.author, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("Profissional", fontSize = 8.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = reply.body, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f))
        }
    }
}
