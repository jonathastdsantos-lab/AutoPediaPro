package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MecanicoViewModel
import com.example.ui.Screen

@Composable
fun LoginScreen(viewModel: MecanicoViewModel) {
    val language by viewModel.activeLanguage.collectAsState()
    var email by remember { mutableStateOf("mecanico@oficina.com.br") }
    var password by remember { mutableStateOf("123456") }
    var errorMessage by remember { mutableStateOf("") }

    val textWelcome = if (language == "PT-BR") "Bem-vindo ao AutoPedia" else "Welcome to AutoPedia"
    val textSubtitle = if (language == "PT-BR") "Manuais • Peças • Diagramas • Soluções\nConhecimento que Move" else "Manuals • Parts • Diagrams • Solutions\nKnowledge that Moves"
    val textEmail = if (language == "PT-BR") "E-mail Profissional" else "Professional Email"
    val textPass = if (language == "PT-BR") "Senha" else "Password"
    val textLoginBtn = if (language == "PT-BR") "Acessar Plataforma" else "Login to Platform"
    val textOr = if (language == "PT-BR") "OU" else "OR"
    val textGoogle = if (language == "PT-BR") "Entrar com o Google" else "Sign in with Google"
    val textApple = if (language == "PT-BR") "Entrar com a Apple" else "Sign in with Apple"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Language Toggle at Top Right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                onClick = { viewModel.toggleLanguage() },
                modifier = Modifier.testTag("lang_toggle")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Change Language",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (language == "PT-BR") "PT" else "EN",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Large Header Logo
        Icon(
            imageVector = Icons.Default.Build,
            contentDescription = "Logo",
            tint = Color(0xFFE53935), // Brand Red
            modifier = Modifier
                .size(72.dp)
                .background(
                    color = Color(0xFFE53935).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Title
        Text(
            text = "AutoPedia",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFF1F5F9), // Slate white
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = textSubtitle,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Email Input
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                errorMessage = ""
            },
            label = { Text(textEmail) },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("email_input"),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Password Input
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = ""
            },
            label = { Text(textPass) },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Senha") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("password_input"),
            shape = RoundedCornerShape(12.dp)
        )

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Login Button
        Button(
            onClick = {
                if (email.trim().isEmpty() || password.trim().isEmpty()) {
                    errorMessage = if (language == "PT-BR") "Por favor preencha todos os campos" else "Please fill all fields"
                } else {
                    viewModel.changeScreen(Screen.HOME)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("login_submit_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = textLoginBtn,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
            )
            Text(
                text = textOr,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Simulated Social Logins
        Button(
            onClick = { viewModel.changeScreen(Screen.HOME) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                )
                .testTag("google_login_button"),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onBackground
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Mock G logo
                Text(
                    text = "G ",
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 18.sp
                )
                Text(
                    text = textGoogle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { viewModel.changeScreen(Screen.HOME) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                )
                .testTag("apple_login_button"),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onBackground
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = " ",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = textApple,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
