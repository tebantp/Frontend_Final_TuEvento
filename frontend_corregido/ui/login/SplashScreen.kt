package com.tuevento.tueventofinal.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigate: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBlue),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "TUEVENTO",
                style = MaterialTheme.typography.displayMedium.copy(
                    color = GoldAccent,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 4.sp
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(color = GoldAccent)
        }
    }

    LaunchedEffect(Unit) {
        delay(1500) // Simulación de carga
        onNavigate()
    }
}
