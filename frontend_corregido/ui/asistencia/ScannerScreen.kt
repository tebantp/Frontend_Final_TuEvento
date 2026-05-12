package com.tuevento.tueventofinal.ui.asistencia

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.CompoundBarcodeView
import com.tuevento.tueventofinal.ui.theme.GoldAccent
import com.tuevento.tueventofinal.ui.theme.PrimaryBlue
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    staffId: Long,
    onBack: () -> Unit,
    viewModel: AsistenciaViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var isScanning by remember { mutableStateOf(true) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { hasCameraPermission = it }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) launcher.launch(Manifest.permission.CAMERA)
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is AsistenciaState.Success -> {
                isScanning = false
                snackbarHostState.showSnackbar("Asistencia registrada: ${(uiState as AsistenciaState.Success).asistencia.usuarioNombre}")
                delay(2500)
                viewModel.resetState()
                isScanning = true
            }
            is AsistenciaState.Error -> {
                isScanning = false
                snackbarHostState.showSnackbar((uiState as AsistenciaState.Error).message)
                delay(3000)
                viewModel.resetState()
                isScanning = true
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            "ACCESS CONTROL", 
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                color = GoldAccent
                            )
                        )
                        Text(
                            "Modo Validador Activo",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.6f))
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = GoldAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Brush.verticalGradient(listOf(PrimaryBlue, Color.Black))),
            contentAlignment = Alignment.Center
        ) {
            if (hasCameraPermission) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(24.dp)
                ) {
                    if (isScanning) {
                        Text(
                            "APUNTE AL CÓDIGO QR",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Box(contentAlignment = Alignment.Center) {
                            // Marco decorativo premium
                            Surface(
                                modifier = Modifier.size(280.dp),
                                color = Color.Transparent,
                                shape = RoundedCornerShape(40.dp),
                                border = BorderStroke(2.dp, GoldAccent.copy(alpha = 0.3f))
                            ) {}
                            
                            Card(
                                modifier = Modifier.size(250.dp),
                                shape = RoundedCornerShape(32.dp),
                                border = BorderStroke(1.dp, GoldAccent),
                                colors = CardDefaults.cardColors(containerColor = Color.Black)
                            ) {
                                AndroidView(
                                    factory = { ctx ->
                                        val activity = ctx.findActivity()
                                        CompoundBarcodeView(ctx).apply {
                                            val capture = CaptureManager(activity, this)
                                            capture.initializeFromIntent(activity.intent, null)
                                            capture.decode()
                                            decodeContinuous { result ->
                                                if (isScanning && result.text != null) {
                                                    viewModel.registrarPorQR(result.text, staffId)
                                                }
                                            }
                                            resume()
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            
                            // Animación de escaneo (simulada con un borde que brilla)
                            Surface(
                                modifier = Modifier.size(260.dp),
                                color = Color.Transparent,
                                shape = RoundedCornerShape(32.dp),
                                border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f))
                            ) {}
                        }
                        
                        Spacer(modifier = Modifier.height(40.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Conexión Encriptada",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    } else {
                        StatusFeedback(uiState)
                    }
                }
            } else {
                PermissionRequestView { launcher.launch(Manifest.permission.CAMERA) }
            }
        }
    }
}

@Composable
fun StatusFeedback(state: AsistenciaState) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp)
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(32.dp))
            .padding(32.dp)
    ) {
        when (state) {
            is AsistenciaState.Loading -> {
                CircularProgressIndicator(color = GoldAccent, modifier = Modifier.size(64.dp), strokeWidth = 6.dp)
                Spacer(modifier = Modifier.height(24.dp))
                Text("PROCESANDO...", fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 2.sp)
            }
            is AsistenciaState.Success -> {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                    modifier = Modifier.size(100.dp),
                    border = BorderStroke(2.dp, Color(0xFF4CAF50))
                ) {
                    Icon(Icons.Default.CheckCircle, "Éxito", tint = Color(0xFF4CAF50), modifier = Modifier.padding(20.dp))
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text("ACCESO PERMITIDO", fontWeight = FontWeight.Black, fontSize = 22.sp, color = Color(0xFF4CAF50), letterSpacing = 1.sp)
                Text(state.asistencia.usuarioNombre.uppercase(), color = Color.White, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold))
                Spacer(modifier = Modifier.height(8.dp))
                Text("INVITACIÓN VÁLIDA", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelMedium)
            }
            is AsistenciaState.Error -> {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFF44336).copy(alpha = 0.1f),
                    modifier = Modifier.size(100.dp),
                    border = BorderStroke(2.dp, Color(0xFFF44336))
                ) {
                    Icon(Icons.Default.Error, "Error", tint = Color(0xFFF44336), modifier = Modifier.padding(20.dp))
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text("ACCESO DENEGADO", fontWeight = FontWeight.Black, fontSize = 22.sp, color = Color(0xFFF44336), letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(state.message.uppercase(), color = Color.White.copy(alpha = 0.8f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
            else -> {}
        }
    }
}

@Composable
fun PermissionRequestView(onRequest: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Se requiere acceso a la cámara", color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRequest,
            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = PrimaryBlue)
        ) {
            Text("CONCEDER PERMISO", fontWeight = FontWeight.Bold)
        }
    }
}

fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("No se pudo encontrar la Activity")
}
