package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.ui.MecanicoViewModel
import com.example.ui.Screen
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel = ViewModelProvider(this)[MecanicoViewModel::class.java]
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val activeLanguage by viewModel.activeLanguage.collectAsState()

            MyApplicationTheme(darkTheme = isDarkMode, dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigationContainer(viewModel)
                }
            }
        }
    }
}

@Composable
fun AppNavigationContainer(viewModel: MecanicoViewModel) {
    val activeScreen by viewModel.activeScreen.collectAsState()
    val unreadNotifs by viewModel.unreadNotificationsCount.collectAsState()
    val language by viewModel.activeLanguage.collectAsState()

    val itemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = Color(0xFFC81E2C), // Exact AutoPedia Brand Red Accent
        selectedTextColor = Color(0xFFF1F5F9), // Light Silver text
        unselectedIconColor = Color(0xFF94A3B8), // Muted Navy Gray
        unselectedTextColor = Color(0xFF94A3B8),
        indicatorColor = Color(0xFF1B2C4E) // Deep Steel Navy container indicator
    )

    val railItemColors = NavigationRailItemDefaults.colors(
        selectedIconColor = Color(0xFFC81E2C), // Exact AutoPedia Brand Red Accent
        selectedTextColor = Color(0xFFF1F5F9), // Light Silver text
        unselectedIconColor = Color(0xFF94A3B8), // Muted Navy Gray
        unselectedTextColor = Color(0xFF94A3B8),
        indicatorColor = Color(0xFF1B2C4E) // Deep Steel Navy container indicator
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWide = maxWidth >= 600.dp
        val showNav = activeScreen != Screen.LOGIN

        if (isWide && showNav) {
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    modifier = Modifier
                        .fillMaxHeight()
                        .testTag("app_side_navigation")
                        .drawBehind {
                            drawLine(
                                color = Color(0xFF1B2C4E), // Muted border
                                start = Offset(size.width, 0f),
                                end = Offset(size.width, size.height),
                                strokeWidth = 1.dp.toPx()
                            )
                        },
                    containerColor = Color(0xFF050A12), // Midnight Navy background
                    header = {
                        Spacer(modifier = Modifier.height(16.dp))
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = "AutoPedia",
                            tint = Color(0xFFC81E2C), // Brand Red
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                ) {
                    NavigationRailItem(
                        selected = activeScreen == Screen.HOME,
                        onClick = { viewModel.changeScreen(Screen.HOME) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text(if (language == "PT-BR") "Início" else "Home") },
                        colors = railItemColors,
                        modifier = Modifier.testTag("nav_home_tab_rail")
                    )

                    NavigationRailItem(
                        selected = activeScreen == Screen.MANUALS,
                        onClick = {
                            viewModel.selectedVehicle.value = null
                            viewModel.changeScreen(Screen.MANUALS)
                        },
                        icon = { Icon(Icons.Default.MenuBook, contentDescription = "Manuals") },
                        label = { Text(if (language == "PT-BR") "Manuais" else "Manuals") },
                        colors = railItemColors,
                        modifier = Modifier.testTag("nav_manuals_tab_rail")
                    )

                    NavigationRailItem(
                        selected = activeScreen == Screen.AI_CHAT,
                        onClick = { viewModel.changeScreen(Screen.AI_CHAT) },
                        icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI") },
                        label = { Text("Doutor IA") },
                        colors = railItemColors,
                        modifier = Modifier.testTag("nav_ai_tab_rail")
                    )

                    NavigationRailItem(
                        selected = activeScreen == Screen.FORUM,
                        onClick = {
                            viewModel.selectedTopic.value = null
                            viewModel.changeScreen(Screen.FORUM)
                        },
                        icon = { Icon(Icons.Default.Forum, contentDescription = "Forum") },
                        label = { Text(if (language == "PT-BR") "Fórum" else "Forum") },
                        colors = railItemColors,
                        modifier = Modifier.testTag("nav_forum_tab_rail")
                    )

                    NavigationRailItem(
                        selected = activeScreen == Screen.TUTORIALS,
                        onClick = { viewModel.changeScreen(Screen.TUTORIALS) },
                        icon = { Icon(Icons.Default.VideoLibrary, contentDescription = "Videos") },
                        label = { Text(if (language == "PT-BR") "Vídeos" else "Videos") },
                        colors = railItemColors,
                        modifier = Modifier.testTag("nav_videos_tab_rail")
                    )

                    NavigationRailItem(
                        selected = activeScreen in listOf(
                            Screen.OFFICE_MAIN, Screen.OFFICE_CREATE, Screen.OFFICE_DASHBOARD,
                            Screen.OFFICE_ADD_VEHICLE, Screen.OFFICE_VEHICLE_DETAIL, Screen.OFFICE_NEW_ATTENDANCE
                        ),
                        onClick = { viewModel.changeScreen(Screen.OFFICE_MAIN) },
                        icon = { Icon(Icons.Default.Storefront, contentDescription = "Oficina") },
                        label = { Text(if (language == "PT-BR") "Oficina" else "Workshop") },
                        colors = railItemColors,
                        modifier = Modifier.testTag("nav_oficina_tab_rail")
                    )

                    NavigationRailItem(
                        selected = activeScreen == Screen.PROFILE,
                        onClick = { viewModel.changeScreen(Screen.PROFILE) },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text(if (language == "PT-BR") "Perfil" else "Profile") },
                        colors = railItemColors,
                        modifier = Modifier.testTag("nav_profile_tab_rail")
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    when (activeScreen) {
                        Screen.LOGIN -> LoginScreen(viewModel)
                        Screen.HOME -> HomeScreen(viewModel)
                        Screen.MANUALS -> ManualsScreen(viewModel)
                        Screen.FORUM -> ForumScreen(viewModel)
                        Screen.TUTORIALS -> TutorialsScreen(viewModel)
                        Screen.PROFILE -> ProfileScreen(viewModel)
                        Screen.AI_CHAT -> AiMechanicScreen(viewModel)
                        Screen.OFFICE_MAIN,
                        Screen.OFFICE_CREATE,
                        Screen.OFFICE_DASHBOARD,
                        Screen.OFFICE_ADD_VEHICLE,
                        Screen.OFFICE_VEHICLE_DETAIL,
                        Screen.OFFICE_NEW_ATTENDANCE -> OficinaScreen(viewModel)
                        Screen.CURATOR_PANEL -> CuratorPanelScreen(viewModel)
                    }
                }
            }
        } else {
            Scaffold(
                bottomBar = {
                    if (showNav) {
                        NavigationBar(
                            modifier = Modifier
                                .testTag("app_bottom_navigation")
                                .drawBehind {
                                    drawLine(
                                        color = Color(0xFF1B2C4E), // Muted Navy Gray border
                                        start = Offset(0f, 0f),
                                        end = Offset(size.width, 0f),
                                        strokeWidth = 1.dp.toPx()
                                    )
                                },
                            containerColor = Color(0xFF050A12), // Midnight Navy background
                            tonalElevation = 0.dp
                        ) {
                            NavigationBarItem(
                                selected = activeScreen == Screen.HOME,
                                onClick = { viewModel.changeScreen(Screen.HOME) },
                                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                label = { Text(if (language == "PT-BR") "Início" else "Home") },
                                colors = itemColors,
                                modifier = Modifier.testTag("nav_home_tab")
                            )

                            NavigationBarItem(
                                selected = activeScreen == Screen.MANUALS,
                                onClick = {
                                    viewModel.selectedVehicle.value = null
                                    viewModel.changeScreen(Screen.MANUALS)
                                },
                                icon = { Icon(Icons.Default.MenuBook, contentDescription = "Manuals") },
                                label = { Text(if (language == "PT-BR") "Manuais" else "Manuals") },
                                colors = itemColors,
                                modifier = Modifier.testTag("nav_manuals_tab")
                            )

                            NavigationBarItem(
                                selected = activeScreen == Screen.AI_CHAT,
                                onClick = { viewModel.changeScreen(Screen.AI_CHAT) },
                                icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI") },
                                label = { Text("Doutor IA") },
                                colors = itemColors,
                                modifier = Modifier.testTag("nav_ai_tab")
                            )

                            NavigationBarItem(
                                selected = activeScreen == Screen.FORUM,
                                onClick = {
                                    viewModel.selectedTopic.value = null
                                    viewModel.changeScreen(Screen.FORUM)
                                },
                                icon = { Icon(Icons.Default.Forum, contentDescription = "Forum") },
                                label = { Text(if (language == "PT-BR") "Fórum" else "Forum") },
                                colors = itemColors,
                                modifier = Modifier.testTag("nav_forum_tab")
                            )

                            NavigationBarItem(
                                selected = activeScreen == Screen.TUTORIALS,
                                onClick = { viewModel.changeScreen(Screen.TUTORIALS) },
                                icon = { Icon(Icons.Default.VideoLibrary, contentDescription = "Videos") },
                                label = { Text(if (language == "PT-BR") "Vídeos" else "Videos") },
                                colors = itemColors,
                                modifier = Modifier.testTag("nav_videos_tab")
                            )

                            NavigationBarItem(
                                selected = activeScreen in listOf(
                                    Screen.OFFICE_MAIN, Screen.OFFICE_CREATE, Screen.OFFICE_DASHBOARD,
                                    Screen.OFFICE_ADD_VEHICLE, Screen.OFFICE_VEHICLE_DETAIL, Screen.OFFICE_NEW_ATTENDANCE
                                ),
                                onClick = { viewModel.changeScreen(Screen.OFFICE_MAIN) },
                                icon = { Icon(Icons.Default.Storefront, contentDescription = "Oficina") },
                                label = { Text(if (language == "PT-BR") "Oficina" else "Workshop") },
                                colors = itemColors,
                                modifier = Modifier.testTag("nav_oficina_tab")
                            )

                            NavigationBarItem(
                                selected = activeScreen == Screen.PROFILE,
                                onClick = { viewModel.changeScreen(Screen.PROFILE) },
                                icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                                label = { Text(if (language == "PT-BR") "Perfil" else "Profile") },
                                colors = itemColors,
                                modifier = Modifier.testTag("nav_profile_tab")
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (activeScreen) {
                        Screen.LOGIN -> LoginScreen(viewModel)
                        Screen.HOME -> HomeScreen(viewModel)
                        Screen.MANUALS -> ManualsScreen(viewModel)
                        Screen.FORUM -> ForumScreen(viewModel)
                        Screen.TUTORIALS -> TutorialsScreen(viewModel)
                        Screen.PROFILE -> ProfileScreen(viewModel)
                        Screen.AI_CHAT -> AiMechanicScreen(viewModel)
                        Screen.OFFICE_MAIN,
                        Screen.OFFICE_CREATE,
                        Screen.OFFICE_DASHBOARD,
                        Screen.OFFICE_ADD_VEHICLE,
                        Screen.OFFICE_VEHICLE_DETAIL,
                        Screen.OFFICE_NEW_ATTENDANCE -> OficinaScreen(viewModel)
                        Screen.CURATOR_PANEL -> CuratorPanelScreen(viewModel)
                    }
                }
            }
        }
    }
}

