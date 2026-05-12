package com.tuevento.tueventofinal.ui.eventos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tuevento.tueventofinal.data.model.EventoRequest
import com.tuevento.tueventofinal.ui.theme.GoldAccent
import com.tuevento.tueventofinal.ui.theme.PrimaryBlue
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventoCrearScreen(
    organizadorId: Long,
    onSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: EventoCrearViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var lugar by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var cupoMaximo by remember { mutableStateOf("") }
    var fechaInicio by remember { mutableStateOf("2025-06-01T10:00:00") } 
    var fechaFin by remember { mutableStateOf("2025-06-01T20:00:00") }

    LaunchedEffect(uiState) {
        when(uiState) {
            is EventoCrearState.Success -> {
                scope.launch {
                    snackbarHostState.showSnackbar("Evento creado exitosamente")
                    kotlinx.coroutines.delay(1500)
                    onSuccess()
                    viewModel.resetState()
                }
            }
            is EventoCrearState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar((uiState as EventoCrearState.Error).message)
                }
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("CREAR EVENTO PREMIUM", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PremiumTextField(value = titulo, onValueChange = { titulo = it }, label = "Título del Evento", icon = Icons.Default.Title)
            PremiumTextField(value = descripcion, onValueChange = { descripcion = it }, label = "Descripción", icon = Icons.Default.Description, singleLine = false)
            PremiumTextField(value = lugar, onValueChange = { lugar = it }, label = "Lugar (Nombre)", icon = Icons.Default.LocationOn)
            PremiumTextField(value = direccion, onValueChange = { direccion = it }, label = "Dirección Exacta", icon = Icons.Default.Map)
            PremiumTextField(value = cupoMaximo, onValueChange = { cupoMaximo = it }, label = "Cupo Máximo", icon = Icons.Default.Group, isNumber = true)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = {
                    if (titulo.isBlank() || lugar.isBlank() || cupoMaximo.isBlank()) {
                        scope.launch { snackbarHostState.showSnackbar("Por favor completa los campos obligatorios") }
                    } else {
                        val request = EventoRequest(
                            titulo = titulo,
                            descripcion = descripcion,
                            fechaInicio = fechaInicio,
                            fechaFin = fechaFin,
                            lugar = lugar,
                            direccion = direccion,
                            cupoMaximo = cupoMaximo.toIntOrNull() ?: 0
                        )
                        viewModel.crearEvento(organizadorId, request)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                enabled = uiState !is EventoCrearState.Loading
            ) {
                if (uiState is EventoCrearState.Loading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("PUBLICAR EVENTO", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }
        }
    }
}

@Composable
fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    singleLine: Boolean = true,
    isNumber: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = GoldAccent) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GoldAccent,
            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
            focusedLabelColor = GoldAccent,
            unfocusedLabelColor = Color.Gray,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        singleLine = singleLine,
        visualTransformation = visualTransformation,
        keyboardOptions = if (isNumber) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default
    )
}
