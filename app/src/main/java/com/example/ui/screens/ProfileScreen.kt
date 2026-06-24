package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MecanicoViewModel
import com.example.ui.Screen
import com.example.data.UserBadge
import com.example.data.SavedItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: MecanicoViewModel) {
    val language by viewModel.activeLanguage.collectAsState()
    val isDark by viewModel.isDarkMode.collectAsState()
    val name by viewModel.userName.collectAsState()
    val role by viewModel.userRole.collectAsState()
    val workshop by viewModel.userWorkshop.collectAsState()

    val badges by viewModel.badges.collectAsState()
    val offlineItems by viewModel.savedItems.collectAsState()
    val contributions by viewModel.allContributions.collectAsState()

    val totalBadges = badges.size
    val unlockedCount = badges.count { it.isUnlocked }

    // Translations
    val labelProfile = if (language == "PT-BR") "Meu Perfil" else "My Profile"
    val labelBadges = if (language == "PT-BR") "Minhas Medalhas" else "My Badges"
    val labelOffline = if (language == "PT-BR") "Salvos para Offline" else "Saved Offline"
    val labelHistory = if (language == "PT-BR") "Histórico de Contribuições" else "Contribution History"
    val labelPreferences = if (language == "PT-BR") "Preferências" else "Preferences"
    val labelTheme = if (language == "PT-BR") "Modo Escuro" else "Dark Mode"
    val labelLanguage = if (language == "PT-BR") "Idioma" else "Language"
    val labelProgress = if (language == "PT-BR") "Desbloqueado" else "Unlocked"
    val labelNoOffline = if (language == "PT-BR") "Nenhum manual salvo offline ainda" else "No manuals saved offline yet"
    val labelNoContribs = if (language == "PT-BR") "Você ainda não fez contribuições" else "You have not made any contributions yet"

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(labelProfile, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Header Info Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131C2D)),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Engineering, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = name, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        Text(text = role, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = workshop,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Dynamic Credibility Seal Card (PROMPT 4)
            item {
                val points by viewModel.calcularPontosCredibilidade(name).collectAsState(initial = 0)
                
                val tierLabel: String
                val tint: Color
                val icon: androidx.compose.ui.graphics.vector.ImageVector
                val nextThreshold: Int
                val nextTier: String

                when {
                    points >= 300 -> {
                        tierLabel = "Mestre de Ouro"
                        tint = Color(0xFFFFD700)
                        icon = Icons.Default.WorkspacePremium
                        nextThreshold = 300
                        nextTier = ""
                    }
                    points >= 150 -> {
                        tierLabel = "Especialista Prata"
                        tint = Color(0xFFC0C0C0)
                        icon = Icons.Default.Stars
                        nextThreshold = 300
                        nextTier = "Mestre de Ouro"
                    }
                    points >= 50 -> {
                        tierLabel = "Colaborador Bronze"
                        tint = Color(0xFFCD7F32)
                        icon = Icons.Default.Shield
                        nextThreshold = 150
                        nextTier = "Especialista Prata"
                    }
                    else -> {
                        tierLabel = "Iniciante"
                        tint = Color(0xFF888888)
                        icon = Icons.Default.Info
                        nextThreshold = 50
                        nextTier = "Colaborador Bronze"
                    }
                }

                val progress = when {
                    points >= 300 -> 1f
                    points >= 150 -> (points - 150).toFloat() / 150f
                    points >= 50 -> (points - 50).toFloat() / 100f
                    else -> points.toFloat() / 50f
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, tint.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .testTag("credibility_seal_card"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131C2D)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(tint.copy(alpha = 0.15f), CircleShape)
                                    .border(1.5.dp, tint, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = tint,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Selo de Credibilidade",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = tierLabel,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = tint
                                )
                                Text(
                                    text = "$points Pontos Acumulados",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (points < 300) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Progresso para $nextTier",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = "$points / $nextThreshold pts",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = tint
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = tint,
                                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                            )
                        } else {
                            Text(
                                text = "Nível Máximo Atingido! Obrigado por guiar nossa comunidade.",
                                fontSize = 10.sp,
                                color = com.example.ui.theme.SuccessGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(8.dp))

                        // Weights details
                        Text(
                            text = "Como pontuar: +10 Dica Crônica | +5 Resposta Útil | +20 Preço Validado",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Dynamic Simulation Toggle Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131C2D)),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Simulação de Papel (Corretor)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (role == "Profissional" || role == "Mecânico de Veículos") 
                                    "Acesso Profissional ativo (Propor/Aprovar edições)" 
                                    else "Acesso Dono de Veículo ativo (Apenas leitura)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Switch(
                            checked = (role == "Profissional" || role == "Mecânico de Veículos"),
                            onCheckedChange = { viewModel.toggleUserRole() },
                            modifier = Modifier.testTag("simulation_role_switch")
                        )
                    }
                }
            }

            // Curator Panel Access Card
            if (role == "Profissional" || role == "Mecânico de Veículos") {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .border(1.dp, Color(0xFF10B981).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .clickable { viewModel.changeScreen(Screen.CURATOR_PANEL) }
                            .testTag("curator_panel_access_card"),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1914)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Painel de Curadoria",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF10B981)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Moderação inteligente de extrações pendentes da web & IA para o catálogo.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowForwardIos,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // Customizable Preferences Area
            item {
                Text(labelPreferences, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131C2D)),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Dark Mode Toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dark_mode_preference_row")
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.Default.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Column {
                                    Text(labelTheme, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Melhorar leitura em oficinas", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }
                            }
                            Switch(
                                checked = isDark,
                                onCheckedChange = { viewModel.toggleDarkMode() },
                                modifier = Modifier.testTag("dark_mode_toggle_switch")
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                        // Language Toggle Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggleLanguage() }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.Default.Translate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Column {
                                    Text(labelLanguage, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Alterar idioma de termos", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }
                            }
                            Text(
                                text = if (language == "PT-BR") "Português (BR)" else "English (US)",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Gamification Achievements & Badges Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = labelBadges,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "$labelProgress: $unlockedCount / $totalBadges",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { unlockedCount.toFloat() / totalBadges.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                // We use a LazyRow or Column to list badges nicely
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(badges) { badge ->
                        BadgeCard(badge = badge)
                    }
                }
            }

            // Offline Saved Items Directory
            item {
                Text(labelOffline, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (offlineItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(labelNoOffline, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                }
            } else {
                items(offlineItems) { item ->
                    SavedOfflineItemRow(item = item, viewModel = viewModel)
                }
            }

            // User Contributions Log
            item {
                Text(labelHistory, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
            }

            val userContribs = contributions.filter { it.authorName == name }
            if (userContribs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(labelNoContribs, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                }
            } else {
                items(userContribs) { contrib ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131C2D)),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = contrib.title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = contrib.body, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun BadgeCard(badge: UserBadge) {
    Card(
        modifier = Modifier
            .width(130.dp)
            .height(140.dp)
            .testTag("badge_card_${badge.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (badge.isUnlocked) Color(0xFF1A2A44) else Color(0xFF131C2D)
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (badge.isUnlocked) BorderStroke(1.5.dp, Color(0xFFE53935)) else BorderStroke(1.dp, Color(0xFF1E2D4A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            val icon = when (badge.iconName) {
                "Handshake" -> Icons.Default.Handshake
                "Build" -> Icons.Default.Build
                "Forum" -> Icons.Default.Forum
                "Book" -> Icons.Default.Book
                else -> Icons.Default.Download
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (badge.isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(36.dp)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = badge.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = badge.description,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    lineHeight = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun SavedOfflineItemRow(
    item: SavedItem,
    viewModel: MecanicoViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131C2D)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (item.type == "MANUAL") Icons.Default.Book else Icons.Default.Forum,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )

                Column {
                    Text(text = item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = item.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }

            IconButton(
                onClick = { viewModel.toggleSaveItem(item.type, item.referenceId, "", "") },
                modifier = Modifier.testTag("remove_offline_save_button")
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Offline Item", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
            }
        }
    }
}
