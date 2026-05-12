package com.tuevento.tueventofinal.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tuevento.tueventofinal.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    eventoId: Long,
    usuarioId: Long,
    onBack: () -> Unit,
    viewModel: ChatViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var nuevoMensaje by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(eventoId) {
        viewModel.fetchMensajes(eventoId)
    }

    // Scroll automatically to bottom when new messages arrive
    LaunchedEffect(uiState) {
        if (uiState is ChatState.Success) {
            val messages = (uiState as ChatState.Success).mensajes
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Chat Inmersivo", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        Text("Evento ID: $eventoId", style = MaterialTheme.typography.labelSmall, color = GoldAccent)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = GoldAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Surface(
                color = PrimaryBlue,
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .navigationBarsPadding()
                        .imePadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = nuevoMensaje,
                        onValueChange = { nuevoMensaje = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Mensaje premium...", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldAccent,
                            unfocusedBorderColor = Color.DarkGray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = GoldAccent
                        ),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FloatingActionButton(
                        onClick = {
                            if (nuevoMensaje.isNotBlank()) {
                                viewModel.enviarMensaje(usuarioId, eventoId, nuevoMensaje)
                                nuevoMensaje = ""
                            }
                        },
                        containerColor = GoldAccent,
                        contentColor = PrimaryBlue,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Enviar")
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(PrimaryBlue, Color.Black)
                    )
                )
        ) {
            when (val state = uiState) {
                is ChatState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = GoldAccent
                    )
                }
                is ChatState.Success -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.mensajes) { mensaje ->
                            ChatBubblePremium(
                                mensaje = mensaje.contenido, 
                                remitente = mensaje.remitenteNombre, 
                                esPropio = mensaje.remitenteId == usuarioId
                            )
                        }
                    }
                }
                is ChatState.Error -> {
                    Text(
                        text = state.message, 
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubblePremium(mensaje: String, remitente: String, esPropio: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (esPropio) Alignment.End else Alignment.Start
    ) {
        if (!esPropio) {
            Text(
                text = remitente, 
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                ), 
                color = GoldAccent,
                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
            )
        }
        Surface(
            color = if (esPropio) GoldAccent else Color(0xFF252525),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (esPropio) 16.dp else 2.dp,
                bottomEnd = if (esPropio) 2.dp else 16.dp
            ),
            tonalElevation = if (esPropio) 4.dp else 0.dp
        ) {
            Text(
                text = mensaje,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                color = if (esPropio) PrimaryBlue else Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
