package com.tuevento.tueventofinal.ui.reportes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tuevento.tueventofinal.ui.theme.GoldAccent
import com.tuevento.tueventofinal.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportesScreen(
    organizadorId: Long,
    onBack: () -> Unit,
    viewModel: ReportesViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(organizadorId) {
        viewModel.loadEstadisticas(organizadorId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportes y Estadísticas", color = GoldAccent) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = GoldAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            when (val state = uiState) {
                is ReportesState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }
                is ReportesState.Success -> {
                    StatsContent(
                        stats = state.stats,
                        onExportCSV = { /* Lógica de exportación */ },
                        onExportPDF = { /* Lógica de exportación */ }
                    )
                }
                is ReportesState.Error -> {
                    Text(text = "Error: ${state.message}", color = Color.Red)
                    Button(onClick = { viewModel.loadEstadisticas(organizadorId) }) {
                        Text("Reintentar")
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun StatsContent(
    stats: Map<String, Any>,
    onExportCSV: () -> Unit,
    onExportPDF: () -> Unit
) {
    Column {
        Text(
            text = "Resumen de Actividad",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = PrimaryBlue
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(stats.toList()) { (key, value) ->
                StatItem(label = key, value = value.toString())
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onExportCSV,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Icon(Icons.Default.Description, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Exportar CSV")
            }
            Button(
                onClick = onExportPDF,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = PrimaryBlue)
            ) {
                Icon(Icons.Default.FileDownload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Exportar PDF")
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontWeight = FontWeight.Medium)
            Text(text = value, fontWeight = FontWeight.Bold, color = PrimaryBlue)
        }
    }
}
