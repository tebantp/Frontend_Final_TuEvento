package com.tuevento.tueventofinal.ui.qr

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRScreen(
    inscripcionId: Long,
    onBack: () -> Unit,
    viewModel: QRViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(inscripcionId) {
        viewModel.fetchQR(inscripcionId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Código QR") },
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
                is QRState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is QRState.Success -> {
                    val qrCode = state.qr.codigoUnico
                    val bitmap = remember(qrCode) {
                        try {
                            val barcodeEncoder = BarcodeEncoder()
                            barcodeEncoder.encodeBitmap(qrCode, BarcodeFormat.QR_CODE, 800, 800)
                        } catch (e: Exception) {
                            null
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "QR Code",
                                modifier = Modifier.size(300.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Evento: ${state.qr.eventoTitulo}")
                        Text(text = "Usuario: ${state.qr.usuarioNombre}")
                        Text(text = "Expira: ${state.qr.fechaExpiracion}")
                    }
                }
                is QRState.Error -> {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}
