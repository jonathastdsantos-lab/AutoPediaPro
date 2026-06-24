package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.ui.MecanicoViewModel
import com.example.ui.VideoTutorial
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorialsScreen(viewModel: MecanicoViewModel) {
    val language by viewModel.activeLanguage.collectAsState()
    val videos = viewModel.videoTutorials

    var selectedVideo by remember { mutableStateOf<VideoTutorial?>(null) }

    // Translations
    val labelTutorials = if (language == "PT-BR") "Tutoriais em Vídeo" else "Video Tutorials"
    val labelSubtitle = if (language == "PT-BR") "Vídeos técnicos exclusivos de mecânica e funilaria" else "Exclusive technical videos for mechanics and bodywork"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (selectedVideo == null) labelTutorials else selectedVideo!!.title,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = if (selectedVideo == null) labelSubtitle else "Por ${selectedVideo!!.author}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                navigationIcon = {
                    if (selectedVideo != null) {
                        IconButton(onClick = { selectedVideo = null }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            if (selectedVideo == null) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(videos) { video ->
                        VideoTutorialCard(video = video) {
                            selectedVideo = video
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            } else {
                MockVideoPlayerView(video = selectedVideo!!)
            }
        }
    }
}

@Composable
fun VideoTutorialCard(
    video: VideoTutorial,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(12.dp))
            .testTag("video_card_${video.id}"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131C2D)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            // Simulated Video Thumbnail Hero
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Color(0xFF1A2A44)) // Brand Deep Navy background
            ) {
                // Background grid lines simulating visual graph
                Icon(
                    imageVector = Icons.Default.PlayCircleFilled,
                    contentDescription = "Play",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(56.dp)
                        .align(Alignment.Center)
                )

                // Category Tag
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = video.category,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Duration Tag
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = video.duration,
                        fontSize = 11.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Info Details
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = video.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "Instrutor: ${video.author}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = video.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
fun MockVideoPlayerView(video: VideoTutorial) {
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0.15f) }
    var soundLevel by remember { mutableStateOf(0.8f) }
    var isMuted by remember { mutableStateOf(false) }

    // Simulating progress bar increase when playing
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(1000)
            if (progress < 1.0f) {
                progress += 0.02f
            } else {
                progress = 0.0f
                isPlaying = false
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Player Container Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    // Animated mock equalizer inside player
                    if (isPlaying) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .height(50.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            // Simple mock equalizer bars
                            repeat(5) {
                                Box(
                                    modifier = Modifier
                                        .width(6.dp)
                                        .fillMaxHeight(0.3f + (0.7f * Math.random().toFloat()))
                                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                                )
                            }
                        }
                    } else {
                        IconButton(
                            onClick = { isPlaying = true },
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(64.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }

                    // Player Controls Bar Overlay
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(8.dp)
                    ) {
                        // Slider progress bar
                        Slider(
                            value = progress,
                            onValueChange = { progress = it },
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = Color.DarkGray
                            ),
                            modifier = Modifier
                                .height(16.dp)
                                .fillMaxWidth()
                        )

                        // Play/Pause, volume, screen ratio buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(
                                    onClick = { isPlaying = !isPlaying },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Toggle Play",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Text(
                                    text = "02:30 / ${video.duration}",
                                    color = Color.White,
                                    fontSize = 11.sp
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(
                                    onClick = { isMuted = !isMuted },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                        contentDescription = "Mute",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Fullscreen,
                                    contentDescription = "Fullscreen",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Details content under video
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "RESUMO DA AULA",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = video.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Categoria: ${video.category} • Autor: ${video.author}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = video.description, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                }
            }
        }

        // Step Guide details card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CONTEÚDO DO VÍDEO & SOLUÇÕES",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    repeat(3) { step ->
                        val (stepTitle, stepDesc) = when (step) {
                            0 -> "Passo 1: Preparação de Ferramental" to "Desligue a ignição do veículo, prepare o multímetro na escala de tensão contínua (DC 20V) e remova a tampa de acabamento superior do motor."
                            1 -> "Passo 2: Verificação do Sinal Elétrico" to "Insira as pontas de prova na fiação do sensor para diagnosticar a modulação por largura de pulso (sinal PWM) oscilando na faixa de trabalho normal."
                            else -> "Passo 3: Montagem e Teste de Carga" to "Reinstale o novo componente aplicando o torque correto especificado em manual e dê partida verificando se as luzes de erro no painel se apagaram."
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (step + 1).toString(),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column {
                                Text(text = stepTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(text = stepDesc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
