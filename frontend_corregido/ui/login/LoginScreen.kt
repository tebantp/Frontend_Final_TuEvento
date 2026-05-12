package com.tuevento.tueventofinal.ui.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tuevento.tueventofinal.data.model.UsuarioResponse
import com.tuevento.tueventofinal.ui.eventos.PremiumTextField
import com.tuevento.tueventofinal.ui.theme.GoldAccent
import com.tuevento.tueventofinal.ui.theme.PrimaryBlue
import com.tuevento.tueventofinal.ui.theme.SecondaryBlue

@Composable
fun LoginScreen(
    onLoginSuccess: (UsuarioResponse) -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: LoginViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    val gradient = Brush.verticalGradient(
        colors = listOf(PrimaryBlue, SecondaryBlue)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "TUEVENTO",
                style = MaterialTheme.typography.displayMedium.copy(
                    color = GoldAccent,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 4.sp
                )
            )
            Text(
                text = "Tu entrada al mundo de los eventos",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White.copy(alpha = 0.7f),
                    letterSpacing = 1.sp
                )
            )
            
            Spacer(modifier = Modifier.height(56.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "BIENVENIDO",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    PremiumTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Correo Electrónico",
                        icon = Icons.Default.Email
                    )

                    PremiumTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Contraseña",
                        icon = Icons.Default.Lock,
                        visualTransformation = PasswordVisualTransformation()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (uiState is LoginState.Loading) {
                        CircularProgressIndicator(color = GoldAccent)
                    } else {
                        Button(
                            onClick = { viewModel.login(email, password) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldAccent,
                                contentColor = PrimaryBlue
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                        ) {
                            Text(
                                "ENTRAR",
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp
                            )
                        }
                    }

                    TextButton(onClick = onNavigateToRegister) {
                        Text(
                            "¿No tienes cuenta? Únete ahora",
                            color = GoldAccent.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (uiState is LoginState.Error) {
                        Text(
                            text = (uiState as LoginState.Error).message,
                            color = Color(0xFFFF4C4C),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is LoginState.Success) {
            onLoginSuccess((uiState as LoginState.Success).user)
        }
    }
}
