package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ChatMessage
import com.example.ui.MecanicoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiMechanicScreen(viewModel: MecanicoViewModel) {
    val language by viewModel.activeLanguage.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isLoading by viewModel.isChatLoading.collectAsState()

    var inputPrompt by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Translations
    val titleAI = if (language == "PT-BR") "Doutor IA AutoPedia" else "AutoPedia AI Doctor"
    val subtitleAI = if (language == "PT-BR") "Diagnóstico e códigos por inteligência artificial" else "Diagnostics and codes by artificial intelligence"
    val inputPlaceholder = if (language == "PT-BR") "Pergunte ao Doutor IA AutoPedia..." else "Ask AutoPedia AI Doctor..."
    val labelClear = if (language == "PT-BR") "Limpar" else "Clear"

    // Suggested quick diagnostic prompt chips
    val suggestions = if (language == "PT-BR") listOf(
        "Como regular o ponto do Gol G5 1.0?",
        "Problema crônico da correia do Onix Turbo",
        "Esquema elétrico do pedal do acelerador Voyage",
        "Por que acende luz do NoX no MB Accelo?"
    ) else listOf(
        "How to sync Gol G5 1.0 timing belt?",
        "Onix Turbo oil-bathed belt chronic issue",
        "Voyage accelerator pedal pinout scheme",
        "Why does NoX light turn on MB Accelo?"
    )

    // Scroll chat to end when new messages arrive
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = titleAI, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(text = subtitleAI, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.clearChat() },
                        modifier = Modifier.testTag("clear_chat_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text(labelClear)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Chat Message list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(chatMessages) { msg ->
                    ChatBubble(message = msg, language = language)
                }

                if (isLoading) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp, 12.dp, 12.dp, 0.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                               ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = if (language == "PT-BR") "Analisando sintomas..." else "Analyzing symptoms...",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Suggestions bar (horizontal scroll chips)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(suggestions) { promptText ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                            .clickable {
                                inputPrompt = promptText
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = promptText,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Input Send bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131C2D)),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputPrompt,
                        onValueChange = { inputPrompt = it },
                        placeholder = { Text(inputPlaceholder, fontSize = 13.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )

                    IconButton(
                        onClick = {
                            if (inputPrompt.isNotBlank()) {
                                viewModel.sendChatMessage(inputPrompt)
                                inputPrompt = ""
                            }
                        },
                        enabled = inputPrompt.isNotBlank() && !isLoading,
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = if (inputPrompt.isNotBlank() && !isLoading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                shape = CircleShape
                            )
                            .testTag("chat_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = if (inputPrompt.isNotBlank() && !isLoading) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage, language: String) {
    val bubbleColor = if (message.isUser) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)

    val textColor = if (message.isUser) MaterialTheme.colorScheme.onPrimary
    else MaterialTheme.colorScheme.onSurfaceVariant

    val alignment = if (message.isUser) Alignment.End else Alignment.Start

    val shape = if (message.isUser) {
        RoundedCornerShape(12.dp, 12.dp, 0.dp, 12.dp)
    } else {
        RoundedCornerShape(12.dp, 12.dp, 12.dp, 0.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .clip(shape)
                .background(bubbleColor)
                .padding(12.dp)
                .widthIn(max = 280.dp)
        ) {
            Column {
                if (!message.isUser) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = if (language == "PT-BR") "Doutor IA AutoPedia" else "AutoPedia AI Doctor",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Text(
                    text = message.text,
                    fontSize = 13.sp,
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
