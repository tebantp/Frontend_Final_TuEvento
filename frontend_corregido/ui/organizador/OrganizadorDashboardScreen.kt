package com.tuevento.tueventofinal.ui.organizador

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tuevento.tueventofinal.data.model.EventoResponse
import com.tuevento.tueventofinal.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizadorDashboardScreen(
    organizadorId: Long,
    viewModel: OrganizadorViewModel,
    onNavigateToCrear: () -> Unit,
    onNavigateToScanner: () -> Unit,
    onNavigateToDetalle: (Long) -> Unit,
    onNavigateToReportes: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadDashboard(organizadorId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("CENTER COMMAND", color = GoldAccent, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue),
                actions = {
                    IconButton(onClick = onNavigateToReportes) {
                        Icon(Icons.Default.Assessment, contentDescription = "Reportes", tint = GoldAccent)
                    }
                    IconButton(onClick = onNavigateToScanner) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Scanner", tint = GoldAccent)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCrear,
                containerColor = GoldAccent,
                contentColor = PrimaryBlue,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear Evento", modifier = Modifier.size(32.dp))
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
                is OrganizadorUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = GoldAccent)
                is OrganizadorUiState.Error -> Text(state.message, color = Color.Red, modifier = Modifier.align(Alignment.Center))
                is OrganizadorUiState.Success -> {
                    DashboardContentPremium(
                        eventos = state.eventos,
                        stats = state.stats,
                        onEventoClick = onNavigateToDetalle
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardContentPremium(
    eventos: List<EventoResponse>,
    stats: Map<String, Any>?,
    onEventoClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text(
                "Métricas de Impacto",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = GoldAccent
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCardPremium(
                    title = "Asistencia",
                    value = "${stats?.get("porcentajeAsistencia") ?: "85"}%",
                    icon = Icons.Default.TrendingUp,
                    modifier = Modifier.weight(1f)
                )
                StatCardPremium(
                    title = "Ventas",
                    value = stats?.get("totalVentas")?.toString() ?: "$1,240",
                    icon = Icons.Default.Payments,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Text(
                "Mis Eventos Activos",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = GoldAccent
            )
        }

        if (eventos.isEmpty()) {
            item {
                Text("No tienes eventos activos en este momento.", color = Color.Gray)
            }
        } else {
            items(eventos) { evento ->
                EventoGestionCardPremium(evento = evento, onClick = { onEventoClick(evento.id) })
            }
        }
    }
}

@Composable
fun StatCardPremium(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Surface(
                shape = CircleShape,
                color = GoldAccent.copy(alpha = 0.1f),
                modifier = Modifier.size(44.dp)
            ) {
                Icon(icon, contentDescription = null, tint = GoldAccent, modifier = Modifier.padding(10.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
            Text(title.uppercase(), fontSize = 10.sp, color = GoldAccent, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
    }
}

@Composable
fun EventoGestionCardPremium(evento: EventoResponse, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    evento.titulo, 
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color.White)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.People, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "${evento.cupoDisponible} / ${evento.cupoMaximo} cupos", 
                        style = MaterialTheme.typography.bodySmall, 
                        color = Color.Gray
                    )
                }
            }
            
            Surface(
                color = if (evento.estado.name == "ACTIVO") Color(0xFF4CAF50).copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    evento.estado.name,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (evento.estado.name == "ACTIVO") Color(0xFF81C784) else Color.LightGray,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = GoldAccent)
        }
    }
}
