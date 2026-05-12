package com.tuevento.tueventofinal.ui.notificaciones

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tuevento.tueventofinal.data.model.NotificacionResponse
import com.tuevento.tueventofinal.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacionScreen(
    usuarioId: Long,
    onBack: () -> Unit,
    onNavigateToEvento: (Long) -> Unit,
    viewModel: NotificacionViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(usuarioId) {
        viewModel.fetchNotificaciones(usuarioId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "CENTRO DE MENSAJES", 
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        )
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = GoldAccent)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White
                )
            )
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
                is NotificacionState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = GoldAccent
                    )
                }
                is NotificacionState.Success -> {
                    if (state.notificaciones.isEmpty()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.NotificationsNone, 
                                contentDescription = null, 
                                modifier = Modifier.size(80.dp), 
                                tint = GoldAccent.copy(alpha = 0.3f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Tu bandeja está vacía", color = Color.Gray, style = MaterialTheme.typography.bodyLarge)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(state.notificaciones) { notificacion ->
                                NotificacionItemPremium(
                                    notificacion = notificacion,
                                    onClick = { 
                                        viewModel.marcarComoLeida(notificacion.id, usuarioId)
                                        notificacion.eventoId?.let { onNavigateToEvento(it) }
                                    }
                                )
                            }
                        }
                    }
                }
                is NotificacionState.Error -> {
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
fun NotificacionItemPremium(notificacion: NotificacionResponse, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notificacion.leida) Color(0xFF1A1A1A) else Color(0xFF252525)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .then(
                        if (notificacion.leida) Modifier.background(Color.DarkGray)
                        else Modifier.background(Brush.linearGradient(listOf(GoldAccent, Color(0xFFFFB300))))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint = if (notificacion.leida) Color.LightGray else PrimaryBlue,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notificacion.titulo, 
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (notificacion.leida) Color.Gray else Color.White
                )
                Text(
                    text = notificacion.mensaje, 
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (notificacion.leida) Color.Gray.copy(alpha = 0.7f) else SoftWhite
                )
                Text(
                    text = notificacion.fechaCreacion,
                    style = MaterialTheme.typography.labelSmall,
                    color = GoldAccent,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            if (!notificacion.leida) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(GoldAccent)
                )
            }
        }
    }
}
