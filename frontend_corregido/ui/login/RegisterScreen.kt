package com.tuevento.tueventofinal.ui.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.tuevento.tueventofinal.data.model.RolUsuario
import com.tuevento.tueventofinal.ui.eventos.PremiumTextField
import com.tuevento.tueventofinal.ui.theme.GoldAccent
import com.tuevento.tueventofinal.ui.theme.PrimaryBlue
import com.tuevento.tueventofinal.ui.theme.SecondaryBlue
import com.tuevento.tueventofinal.util.ValidationUtils

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel
) {
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var selectedRol by remember { mutableStateOf(RolUsuario.USUARIO) }

    var nombreError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var telefonoError by remember { mutableStateOf<String?>(null) }

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
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "CREAR CUENTA",
                style = MaterialTheme.typography.displaySmall.copy(
                    color = GoldAccent,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
            )
            Text(
                text = "Únete a la mejor comunidad de eventos",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.7f),
                    letterSpacing = 1.sp
                )
            )
            
            Spacer(modifier = Modifier.height(40.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TIPO DE CUENTA",
                        color = GoldAccent,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RolUsuario.entries.filter { it != RolUsuario.STAFF }.forEach { rol ->
                            val isSelected = selectedRol == rol
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedRol = rol },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) GoldAccent else Color.White.copy(alpha = 0.05f),
                                border = if (isSelected) null else BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                            ) {
                                Text(
                                    text = rol.name,
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) PrimaryBlue else Color.White
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Column {
                        PremiumTextField(
                            value = nombre,
                            onValueChange = { 
                                nombre = it
                                nombreError = if (it.isBlank()) "El nombre es obligatorio" else null
                            },
                            label = "Nombre",
                            icon = Icons.Default.Person
                        )
                        nombreError?.let { Text(it, color = Color(0xFFFF4C4C), style = MaterialTheme.typography.bodySmall) }
                    }

                    PremiumTextField(
                        value = apellido,
                        onValueChange = { apellido = it },
                        label = "Apellido",
                        icon = Icons.Default.Badge
                    )

                    Column {
                        PremiumTextField(
                            value = email,
                            onValueChange = { 
                                email = it
                                emailError = when {
                                    !ValidationUtils.isValidEmail(it) -> "Formato de email inválido"
                                    (selectedRol == RolUsuario.ADMINISTRADOR || selectedRol == RolUsuario.ORGANIZADOR) && 
                                    !ValidationUtils.isUdecEmail(it) -> "Se requiere correo institucional @ucundinamarca.edu.co"
                                    else -> null
                                }
                            },
                            label = "Correo Electrónico",
                            icon = Icons.Default.Email
                        )
                        emailError?.let { Text(it, color = Color(0xFFFF4C4C), style = MaterialTheme.typography.bodySmall) }
                    }

                    Column {
                        PremiumTextField(
                            value = password,
                            onValueChange = { 
                                password = it
                                passwordError = if (!ValidationUtils.isValidPassword(it)) "Mín. 8 caracteres, 1 mayúscula y 1 número" else null
                            },
                            label = "Contraseña",
                            icon = Icons.Default.Lock,
                            visualTransformation = PasswordVisualTransformation()
                        )
                        passwordError?.let { Text(it, color = Color(0xFFFF4C4C), style = MaterialTheme.typography.bodySmall) }
                    }

                    Column {
                        PremiumTextField(
                            value = telefono,
                            onValueChange = { 
                                if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                                    telefono = it
                                    telefonoError = if (it.length < 10) "Debe tener 10 dígitos" else null
                                }
                            },
                            label = "Teléfono",
                            icon = Icons.Default.Phone,
                            isNumber = true
                        )
                        telefonoError?.let { Text(it, color = Color(0xFFFF4C4C), style = MaterialTheme.typography.bodySmall) }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (uiState is RegisterState.Loading) {
                        CircularProgressIndicator(color = GoldAccent)
                    } else {
                        Button(
                            onClick = { 
                                // Re-validar todo antes de enviar
                                val isBasicEmailValid = ValidationUtils.isValidEmail(email)
                                val isUdecValid = if (selectedRol == RolUsuario.ADMINISTRADOR || selectedRol == RolUsuario.ORGANIZADOR) {
                                    ValidationUtils.isUdecEmail(email)
                                } else true
                                
                                val isEmailValid = isBasicEmailValid && isUdecValid
                                val isPassValid = ValidationUtils.isValidPassword(password)
                                val isPhoneValid = ValidationUtils.isValidPhone(telefono)
                                val isNombreValid = nombre.isNotBlank()

                                if (isEmailValid && isPassValid && isPhoneValid && isNombreValid) {
                                    viewModel.register(nombre, apellido, email, password, telefono, selectedRol)
                                } else {
                                    nombreError = if (!isNombreValid) "El nombre es obligatorio" else null
                                    emailError = when {
                                        !isBasicEmailValid -> "Verifica tu email"
                                        !isUdecValid -> "Usa el correo institucional @ucundinamarca.edu.co"
                                        else -> null
                                    }
                                    passwordError = if (!isPassValid) "Contraseña no cumple requisitos" else null
                                    telefonoError = if (!isPhoneValid) "Teléfono debe ser de 10 dígitos" else null
                                }
                            },
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
                                "REGISTRARME",
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.5.sp
                            )
                        }
                    }

                    TextButton(onClick = onNavigateToLogin) {
                        Text(
                            "¿Ya tienes cuenta? Inicia sesión",
                            color = GoldAccent.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (uiState is RegisterState.Error) {
                        Text(
                            text = (uiState as RegisterState.Error).message,
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
        if (uiState is RegisterState.Success) {
            onRegisterSuccess()
        }
    }
}
