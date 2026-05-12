package com.tuevento.tueventofinal.ui.perfil

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tuevento.tueventofinal.data.model.InscripcionResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InscripcionesScreen(
    usuarioId: Long,
    onInscripcionClick: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: InscripcionViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(usuarioId) {
        viewModel.fetchInscripciones(usuarioId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Inscripciones") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is InscripcionListState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is InscripcionListState.Success -> {
                    if (state.inscripciones.isEmpty()) {
                        Text(text = "No tienes inscripciones", modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn {
                            items(state.inscripciones) { inscripcion ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                        .clickable { onInscripcionClick(inscripcion.id) }
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(text = inscripcion.eventoTitulo, style = MaterialTheme.typography.titleMedium)
                                        Text(text = "Estado: ${inscripcion.estado}")
                                        Text(text = "Fecha: ${inscripcion.fechaInscripcion}")
                                    }
                                }
                            }
                        }
                    }
                }
                is InscripcionListState.Error -> {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}
