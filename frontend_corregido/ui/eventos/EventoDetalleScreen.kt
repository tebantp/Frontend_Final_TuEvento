package com.tuevento.tueventofinal.ui.eventos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.tuevento.tueventofinal.data.model.FeedbackRequest
import com.tuevento.tueventofinal.data.model.InscripcionRequest
import com.tuevento.tueventofinal.data.remote.FeedbackRepository
import com.tuevento.tueventofinal.data.remote.InscripcionRepository
import com.tuevento.tueventofinal.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun EventoDetalleScreen(
    eventoId: Long,
    usuarioId: Long,
    onBack: () -> Unit,
    viewModel: EventoDetalleViewModel,
    onNavigateToChat: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val inscripcionRepo = remember { InscripcionRepository() }

    LaunchedEffect(eventoId, usuarioId) {
        viewModel.fetchEvento(eventoId, usuarioId)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (uiState is EventoDetalleState.Success) {
                val state = uiState as EventoDetalleState.Success
                val yaInscrito = state.userInscripcion != null

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = PrimaryBlue,
                    tonalElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onNavigateToChat(eventoId) },
                            modifier = Modifier.height(56.dp).weight(0.3f),
                            shape = RoundedCornerShape(16.dp),
                            border = ButtonDefaults.outlinedButtonBorder(true).copy(brush = Brush.linearGradient(listOf(GoldAccent, Color.Yellow)))
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = "Chat", tint = GoldAccent)
                        }

                        Button(
                            onClick = {
                                if (!yaInscrito) {
                                    scope.launch {
                                        try {
                                            val response = inscripcionRepo.createInscripcion(
                                                InscripcionRequest(eventoId, usuarioId)
                                            )
                                            if (response.isSuccessful) {
                                                snackbarHostState.showSnackbar("¡Inscripción exitosa!")
                                                viewModel.fetchEvento(eventoId, usuarioId)
                                            } else {
                                                snackbarHostState.showSnackbar("Error: ${response.code()}")
                                            }
                                        } catch (e: Exception) {
                                            snackbarHostState.showSnackbar("Error de conexión")
                                        }
                                    }
                                }
                            },
                            enabled = !yaInscrito,
                            modifier = Modifier
                                .weight(0.7f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (yaInscrito) Color.Gray else GoldAccent,
                                contentColor = PrimaryBlue
                            )
                        ) {
                            Text(
                                if (yaInscrito) "YA ESTÁS INSCRITO" else "RESERVAR MI LUGAR",
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(PrimaryBlue)
        ) {
            when (val state = uiState) {
                is EventoDetalleState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = GoldAccent)
                }
                is EventoDetalleState.Success -> {
                    val evento = state.evento
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Header Image
                        Box(modifier = Modifier.height(350.dp).fillMaxWidth()) {
                            GlideImage(
                                model = "https://picsum.photos/seed/${evento.id}/1200/800",
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            
                            // Top controls overlay
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                FloatingActionButton(
                                    onClick = onBack,
                                    containerColor = Color.Black.copy(alpha = 0.5f),
                                    contentColor = Color.White,
                                    modifier = Modifier.size(40.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                                }
                                FloatingActionButton(
                                    onClick = { /* Share */ },
                                    containerColor = Color.Black.copy(alpha = 0.5f),
                                    contentColor = Color.White,
                                    modifier = Modifier.size(40.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Compartir")
                                }
                            }

                            // Gradient transition
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, PrimaryBlue),
                                            startY = 700f
                                        )
                                    )
                            )
                        }

                        // Content
                        Column(modifier = Modifier.padding(24.dp)) {
                            Surface(
                                color = GoldAccent.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp),
                                border = ButtonDefaults.outlinedButtonBorder(true).copy(brush = Brush.linearGradient(listOf(GoldAccent, Color.Transparent)))
                            ) {
                                Text(
                                    text = evento.estado.name,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = GoldAccent
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = evento.titulo,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))

                            // Info Row: Date
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = Color.White.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.size(52.dp)
                                ) {
                                    Icon(
                                        Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        modifier = Modifier.padding(14.dp),
                                        tint = GoldAccent
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(text = "CUÁNDO", style = MaterialTheme.typography.labelSmall, color = GoldAccent, letterSpacing = 1.sp)
                                    Text(text = evento.fechaInicio, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = Color.White)
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Info Row: Location
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = Color.White.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.size(52.dp)
                                ) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        modifier = Modifier.padding(14.dp),
                                        tint = GoldAccent
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(text = "DÓNDE", style = MaterialTheme.typography.labelSmall, color = GoldAccent, letterSpacing = 1.sp)
                                    Text(text = evento.lugar, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = Color.White)
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                            Spacer(modifier = Modifier.height(32.dp))

                            Text(
                                text = "Acerca del evento",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = evento.descripcion ?: "Disfruta de una experiencia exclusiva diseñada para los amantes de los eventos de alta calidad.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = SoftWhite.copy(alpha = 0.8f),
                                lineHeight = 28.sp
                            )
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            // Organizer info
                            Text(
                                text = "Host del Evento",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                                    .padding(12.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.size(48.dp),
                                    shape = RoundedCornerShape(24.dp),
                                    color = GoldAccent
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = evento.organizadorNombre.take(1),
                                            color = PrimaryBlue,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = evento.organizadorNombre, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(text = "Organizador Verificado", style = MaterialTheme.typography.labelSmall, color = GoldAccent)
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            // --- SECCIÓN DE FEEDBACK ---
                            Text(
                                text = "Reseñas de la Comunidad",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            if (state.feedback.isEmpty()) {
                                Text("Aún no hay reseñas. ¡Sé el primero en compartir tu experiencia!", color = Color.Gray, fontSize = 14.sp)
                            } else {
                                state.feedback.forEach { feedback ->
                                    FeedbackItemPremium(feedback)
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }

                            if (state.userInscripcion != null) {
                                Spacer(modifier = Modifier.height(24.dp))
                                var showFeedbackDialog by remember { mutableStateOf(false) }
                                Button(
                                    onClick = { showFeedbackDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("ESCRIBIR UNA RESEÑA", color = GoldAccent, fontWeight = FontWeight.Bold)
                                }

                                if (showFeedbackDialog) {
                                    FeedbackDialogPremium(
                                        onDismiss = { showFeedbackDialog = false },
                                        onSubmit = { calificacion, comentario ->
                                            scope.launch {
                                                try {
                                                    val feedbackRepo = FeedbackRepository()
                                                    val res = feedbackRepo.createFeedback(
                                                        FeedbackRequest(state.userInscripcion.id, calificacion, comentario)
                                                    )
                                                    if (res.isSuccessful) {
                                                        viewModel.fetchEvento(eventoId, usuarioId)
                                                        showFeedbackDialog = false
                                                    }
                                                } catch (e: Exception) {
                                                    // Handle error
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(120.dp)) // Extra space for bottom bar
                        }
                    }
                }
                is EventoDetalleState.Error -> {
                    Text(text = state.message, color = Color.Red, modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
fun FeedbackItemPremium(feedback: com.tuevento.tueventofinal.data.model.FeedbackResponse) {
    Surface(
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(feedback.usuarioNombre, fontWeight = FontWeight.Bold, color = Color.White)
                Row {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < feedback.calificacion) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = if (index < feedback.calificacion) GoldAccent else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                feedback.comentario ?: "Sin comentario.",
                color = SoftWhite.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                feedback.fechaEnvio.take(10),
                color = Color.Gray,
                fontSize = 10.sp,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
fun FeedbackDialogPremium(
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit
) {
    var rating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PrimaryBlue,
        title = { Text("Tu Experiencia", color = GoldAccent, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("¿Cómo calificarías este evento?", color = Color.White, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    repeat(5) { index ->
                        IconButton(onClick = { rating = index + 1 }) {
                            Icon(
                                imageVector = if (index < rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = if (index < rating) GoldAccent else Color.Gray,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comentario (opcional)", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = GoldAccent,
                        unfocusedBorderColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(rating, comment) },
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = PrimaryBlue)
            ) {
                Text("ENVIAR RESEÑA", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR", color = Color.Gray)
            }
        }
    )
}
