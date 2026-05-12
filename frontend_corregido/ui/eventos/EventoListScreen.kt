package com.tuevento.tueventofinal.ui.eventos

import androidx.compose.foundation.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.tuevento.tueventofinal.data.model.EventoResponse
import com.tuevento.tueventofinal.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventoListScreen(
    userRole: String = "USUARIO",
    onEventoClick: (Long) -> Unit,
    onAddEventoClick: () -> Unit,
    onScannerClick: () -> Unit,
    onPerfilClick: () -> Unit,
    viewModel: EventoViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val categories = listOf("Todos", "Música", "Tecnología", "Deportes", "Cultura")
    var selectedCategory by remember { mutableStateOf("Todos") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "EXPLORAR",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                color = GoldAccent
                            )
                        )
                        Text(
                            "Eventos exclusivos para ti",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.6f))
                        )
                    }
                },
                actions = {
                    if (userRole != "USUARIO") {
                        IconButton(onClick = onScannerClick) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "Scanner", tint = GoldAccent)
                        }
                    }
                    IconButton(onClick = onPerfilClick) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.1f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = "Perfil",
                                tint = Color.White,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            if (userRole == "ORGANIZADOR" || userRole == "ADMINISTRADOR") {
                FloatingActionButton(
                    onClick = onAddEventoClick,
                    containerColor = GoldAccent,
                    contentColor = PrimaryBlue,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Crear Evento", modifier = Modifier.size(32.dp))
                }
            }
        },
        containerColor = PrimaryBlue
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(PrimaryBlue, Color.Black)))
        ) {
            when (val state = uiState) {
                is EventoListState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = GoldAccent)
                }
                is EventoListState.Success -> {
                    // Lógica de filtrado por categoría
                    val filteredEventos = remember(selectedCategory, state.eventos) {
                        if (selectedCategory == "Todos") state.eventos
                        else state.eventos.filter { evento ->
                            when (selectedCategory) {
                                "Música" -> evento.titulo.contains("Música", ignoreCase = true) ||
                                           (evento.descripcion?.contains("Música", ignoreCase = true) == true)
                                "Tecnología" -> evento.titulo.contains("Hackathon", ignoreCase = true) ||
                                               evento.titulo.contains("Ingeniería", ignoreCase = true) ||
                                               evento.titulo.contains("XPIN", ignoreCase = true) ||
                                               evento.titulo.contains("Software", ignoreCase = true)
                                "Deportes" -> evento.titulo.contains("Torneo", ignoreCase = true) ||
                                             evento.titulo.contains("Voleibol", ignoreCase = true) ||
                                             evento.titulo.contains("Futsal", ignoreCase = true) ||
                                             evento.titulo.contains("Basketball", ignoreCase = true)
                                "Cultura" -> evento.titulo.contains("Cultura", ignoreCase = true) ||
                                            (evento.descripcion?.contains("Cultura", ignoreCase = true) == true)
                                else -> true
                            }
                        }
                    }

                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        item {
                            // Categorías tipo Pill con scroll horizontal
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                categories.forEach { category ->
                                    CategoryPill(
                                        text = category,
                                        selected = selectedCategory == category,
                                        onClick = { selectedCategory = category }
                                    )
                                }
                            }
                        }

                        if (filteredEventos.isNotEmpty()) {
                            item {
                                Text(
                                    "Destacados",
                                    modifier = Modifier.padding(horizontal = 20.dp),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                // Hero Event
                                HeroEventoCard(evento = filteredEventos.first(), onClick = { onEventoClick(filteredEventos.first().id) })
                            }

                            item {
                                Text(
                                    "Próximos Eventos",
                                    modifier = Modifier.padding(horizontal = 20.dp),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                            }

                            items(filteredEventos.drop(1)) { evento ->
                                PremiumEventoItem(evento = evento, onClick = { onEventoClick(evento.id) })
                            }
                        } else {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 60.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "No hay eventos en esta categoría",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color.White.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
                is EventoListState.Error -> {
                    Text(state.message, color = Color.Red, modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
fun CategoryPill(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (selected) GoldAccent else Color.White.copy(alpha = 0.05f),
        border = if (!selected) BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)) else null
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) PrimaryBlue else Color.White,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun HeroEventoCard(evento: EventoResponse, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(32.dp),
    ) {
        Box {
            GlideImage(
                model = "https://picsum.photos/seed/${evento.id}/1000/600",
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp)
            ) {
                Surface(
                    color = GoldAccent,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "DESTACADO",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }
                Text(
                    text = evento.titulo,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun PremiumEventoItem(evento: EventoResponse, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.width(130.dp).fillMaxHeight()) {
                GlideImage(
                    model = "https://picsum.photos/seed/${evento.id}/400/600",
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(1f)
            ) {
                Text(
                    text = evento.titulo,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = evento.lugar, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "FREE", // O el precio si hubiera
                        color = GoldAccent,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                    Surface(
                        color = Color.White.copy(alpha = 0.05f),
                        shape = CircleShape
                    ) {
                        Icon(
                            Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.padding(8.dp).size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
