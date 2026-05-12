package com.tuevento.tueventofinal.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tuevento.tueventofinal.data.model.EventoResponse
import com.tuevento.tueventofinal.data.model.RolUsuario
import com.tuevento.tueventofinal.data.model.UsuarioResponse
import com.tuevento.tueventofinal.util.ValidationUtils
import com.tuevento.tueventofinal.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.loadAdminData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "ADMIN COMMAND", 
                        color = GoldAccent, 
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold, 
                            letterSpacing = 2.sp
                        )
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue),
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Cerrar Sesión", tint = GoldAccent)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(PrimaryBlue, Color.Black)))
        ) {
            TabRow(
                selectedTabIndex = selectedTab, 
                containerColor = Color.Transparent, 
                contentColor = GoldAccent,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = GoldAccent
                    )
                },
                divider = {}
            ) {
                AdminTab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, icon = Icons.AutoMirrored.Filled.EventNote, label = "Eventos")
                AdminTab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, icon = Icons.Default.People, label = "Usuarios")
                AdminTab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, icon = Icons.Default.Insights, label = "Global")
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is AdminUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = GoldAccent)
                    is AdminUiState.Error -> Text(state.message, color = Color.Red, modifier = Modifier.align(Alignment.Center))
                    is AdminUiState.Success -> {
                        when (selectedTab) {
                            0 -> ModeracionListPremium(state.eventosPendientes, onModerar = viewModel::moderar)
                            1 -> UsuariosListPremium(
                                state.usuarios, 
                                onBan = viewModel::banearUsuario,
                                onChangeRol = viewModel::cambiarRolUsuario
                            )
                            2 -> AnaliticaGlobalPremium(state.globalStats)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminTab(selected: Boolean, onClick: () -> Unit, icon: ImageVector, label: String) {
    Tab(
        selected = selected,
        onClick = onClick,
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(icon, contentDescription = null, tint = if (selected) GoldAccent else Color.Gray.copy(alpha = 0.6f))
                Text(label, style = MaterialTheme.typography.labelSmall, color = if (selected) Color.White else Color.Gray)
            }
        }
    )
}

@Composable
fun ModeracionListPremium(eventos: List<EventoResponse>, onModerar: (Long, Boolean) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (eventos.isEmpty()) {
            item {
                Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay eventos pendientes de moderación", color = Color.Gray)
                }
            }
        } else {
            items(eventos) { evento ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = GoldAccent.copy(alpha = 0.1f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(Icons.Default.PendingActions, contentDescription = null, tint = GoldAccent, modifier = Modifier.padding(8.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(evento.titulo, fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 20.sp)
                                Text("Organizador: ${evento.organizadorNombre}", color = GoldAccent, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(evento.descripcion ?: "Sin descripción", color = SoftWhite.copy(alpha = 0.7f), maxLines = 3, fontSize = 14.sp, lineHeight = 20.sp)
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { onModerar(evento.id, true) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) { Text("APROBAR", fontWeight = FontWeight.Black, letterSpacing = 1.sp) }
                            
                            OutlinedButton(
                                onClick = { onModerar(evento.id, false) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF5350)),
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, Color(0xFFEF5350).copy(alpha = 0.5f))
                            ) { Text("RECHAZAR", fontWeight = FontWeight.Bold) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UsuariosListPremium(
    usuarios: List<UsuarioResponse>, 
    onBan: (Long, Boolean) -> Unit,
    onChangeRol: (Long, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(usuarios) { usuario ->
            var showRoleDialog by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (usuario.activo) Color(0xFF4CAF50).copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (usuario.activo) Icons.Default.Person else Icons.Default.PersonOff,
                            contentDescription = null,
                            tint = if (usuario.activo) Color(0xFF81C784) else Color(0xFFEF5350),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "${usuario.nombre} ${usuario.apellido}", 
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                        )
                        Text(usuario.email, color = Color.Gray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            onClick = { showRoleDialog = true },
                            color = GoldAccent.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                usuario.rol.name, 
                                color = GoldAccent, 
                                fontSize = 10.sp, 
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            if (usuario.activo) "ACTIVO" else "BLOQUEADO",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (usuario.activo) Color(0xFF81C784) else Color(0xFFEF5350)
                        )
                        Switch(
                            checked = usuario.activo,
                            onCheckedChange = { onBan(usuario.id, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = GoldAccent,
                                checkedTrackColor = GoldAccent.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }

            if (showRoleDialog) {
                AlertDialog(
                    onDismissRequest = { showRoleDialog = false },
                    title = { Text("Cambiar Rol a ${usuario.nombre}", color = Color.White) },
                    containerColor = Color(0xFF1A1A1A),
                    text = {
                        Column {
                            RolUsuario.entries.forEach { rol ->
                                val isUdec = ValidationUtils.isUdecEmail(usuario.email)
                                val isPrivileged = rol == RolUsuario.ADMINISTRADOR || rol == RolUsuario.ORGANIZADOR
                                val enabled = !isPrivileged || isUdec

                                TextButton(
                                    onClick = { 
                                        onChangeRol(usuario.id, rol.name)
                                        showRoleDialog = false 
                                    },
                                    enabled = enabled,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = if (isPrivileged && !isUdec) "${rol.name} (Solo @ucundinamarca)" else rol.name,
                                        color = when {
                                            usuario.rol == rol -> GoldAccent
                                            !enabled -> Color.Gray
                                            else -> Color.White
                                        }
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showRoleDialog = false }) { 
                            Text("CANCELAR", color = GoldAccent) 
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AnaliticaGlobalPremium(stats: Map<String, Any>?) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = GoldAccent.copy(alpha = 0.05f),
                border = BorderStroke(2.dp, GoldAccent.copy(alpha = 0.2f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(48.dp), tint = GoldAccent)
                }
            }
        }
        
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "REPORTE DE ECOSISTEMA", 
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, letterSpacing = 2.sp), 
                    color = Color.White
                )
                Text(
                    "Métricas de rendimiento global", 
                    color = GoldAccent.copy(alpha = 0.6f), 
                    fontSize = 12.sp, 
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AdminStatCard(
                    label = "Usuarios",
                    value = stats?.get("usuariosTotales")?.toString() ?: "0",
                    icon = Icons.Default.Groups,
                    modifier = Modifier.weight(1f)
                )
                AdminStatCard(
                    label = "Eventos",
                    value = stats?.get("eventosActivos")?.toString() ?: "0",
                    icon = Icons.Default.AutoGraph,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = GoldAccent)
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Crecimiento Mensual", color = PrimaryBlue, fontWeight = FontWeight.Bold)
                        Text("+12% desde el último mes", color = PrimaryBlue.copy(alpha = 0.7f), fontSize = 12.sp)
                    }
                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(32.dp))
                }
            }
        }
    }
}

@Composable
fun AdminStatCard(label: String, value: String, icon: ImageVector, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Icon(icon, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
            Text(label, color = Color.Gray, fontSize = 12.sp)
        }
    }
}
