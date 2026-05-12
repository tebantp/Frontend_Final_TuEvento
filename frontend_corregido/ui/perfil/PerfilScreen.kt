package com.tuevento.tueventofinal.ui.perfil

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.tuevento.tueventofinal.data.model.UsuarioRequest
import com.tuevento.tueventofinal.ui.eventos.PremiumTextField
import com.tuevento.tueventofinal.ui.theme.PrimaryBlue
import com.tuevento.tueventofinal.ui.theme.SecondaryBlue
import com.tuevento.tueventofinal.ui.theme.GoldAccent
import com.tuevento.tueventofinal.util.ValidationUtils
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun PerfilScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: PerfilViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val usuario by viewModel.usuario.collectAsState()
    val context = LocalContext.current

    var nombre by remember(usuario) { mutableStateOf(usuario?.nombre ?: "") }
    var apellido by remember(usuario) { mutableStateOf(usuario?.apellido ?: "") }
    var email by remember(usuario) { mutableStateOf(usuario?.email ?: "") }
    var telefono by remember(usuario) { mutableStateOf(usuario?.telefono ?: "") }
    var password by remember { mutableStateOf("") }

    var emailError by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                val file = uriToFile(it, context)
                if (file != null && usuario != null) {
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("foto", file.name, requestFile)
                    viewModel.subirFoto(usuario!!.id, body)
                }
            }
        }
    )

    LaunchedEffect(uiState) {
        if (uiState is PerfilState.Success) {
            viewModel.resetState()
        }
    }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(PrimaryBlue, SecondaryBlue)
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "MI PERFIL PREMIUM",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp,
                            color = GoldAccent
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = GoldAccent)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.logout()
                        onLogout()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Cerrar Sesión",
                            tint = Color(0xFFFF4C4C)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = PrimaryBlue.copy(alpha = 0.9f)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Foto de Perfil con botón de edición
                Box(
                    modifier = Modifier.size(140.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .align(Alignment.Center)
                            .border(BorderStroke(3.dp, GoldAccent), CircleShape)
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        GlideImage(
                            model = if (usuario?.photoUrl.isNullOrEmpty())
                                "https://ui-avatars.com/api/?name=${usuario?.nombre}+${usuario?.apellido}&background=0F2027&color=FFD700"
                            else usuario?.photoUrl,
                            contentDescription = "Foto de perfil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    
                    // Botón de Edición flotante
                    SmallFloatingActionButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        containerColor = GoldAccent,
                        contentColor = PrimaryBlue,
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Cambiar foto", modifier = Modifier.size(20.dp))
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${usuario?.nombre} ${usuario?.apellido}".uppercase(),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Surface(
                        color = GoldAccent,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        val roleText = when (usuario?.rol) {
                            com.tuevento.tueventofinal.data.model.RolUsuario.ADMINISTRADOR -> "ADMIN"
                            com.tuevento.tueventofinal.data.model.RolUsuario.ORGANIZADOR -> "ORGANIZADOR"
                            com.tuevento.tueventofinal.data.model.RolUsuario.STAFF -> "STAFF"
                            else -> "USUARIO"
                        }
                        Text(
                            text = roleText,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryBlue
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Card para el formulario
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        PremiumTextField(value = nombre, onValueChange = { nombre = it }, label = "Nombre", icon = Icons.Default.Person)
                        PremiumTextField(value = apellido, onValueChange = { apellido = it }, label = "Apellido", icon = Icons.Default.Badge)
                        Column {
                            PremiumTextField(
                                value = email,
                                onValueChange = {
                                    email = it
                                    emailError = when {
                                        !ValidationUtils.isValidEmail(it) -> "Formato de email inválido"
                                        (usuario?.rol == com.tuevento.tueventofinal.data.model.RolUsuario.ADMINISTRADOR ||
                                                usuario?.rol == com.tuevento.tueventofinal.data.model.RolUsuario.ORGANIZADOR) &&
                                                !ValidationUtils.isUdecEmail(it) -> "Se requiere correo institucional @ucundinamarca.edu.co"
                                        else -> null
                                    }
                                },
                                label = "Email",
                                icon = Icons.Default.Email
                            )
                            emailError?.let { Text(it, color = Color(0xFFFF4C4C), style = MaterialTheme.typography.bodySmall) }
                        }
                        PremiumTextField(value = telefono, onValueChange = { telefono = it }, label = "Teléfono", icon = Icons.Default.Phone)
                        PremiumTextField(value = password, onValueChange = { password = it }, label = "Nueva Contraseña (opcional)", icon = Icons.Default.Lock)
                    }
                }

                Button(
                    onClick = {
                        val isBasicEmailValid = ValidationUtils.isValidEmail(email)
                        val isUdecValid = if (usuario?.rol == com.tuevento.tueventofinal.data.model.RolUsuario.ADMINISTRADOR ||
                            usuario?.rol == com.tuevento.tueventofinal.data.model.RolUsuario.ORGANIZADOR
                        ) {
                            ValidationUtils.isUdecEmail(email)
                        } else true

                        if (isBasicEmailValid && isUdecValid) {
                            val request = UsuarioRequest(
                                nombre = nombre,
                                apellido = apellido,
                                email = email,
                                password = password.ifEmpty { "no_change" },
                                telefono = telefono
                            )
                            usuario?.let { viewModel.actualizarPerfil(it.id, request) }
                        } else {
                            emailError = when {
                                !isBasicEmailValid -> "Verifica tu email"
                                !isUdecValid -> "Usa el correo institucional @ucundinamarca.edu.co"
                                else -> null
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldAccent,
                        contentColor = PrimaryBlue
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                    enabled = uiState !is PerfilState.Loading
                ) {
                    if (uiState is PerfilState.Loading) {
                        CircularProgressIndicator(color = PrimaryBlue, modifier = Modifier.size(24.dp))
                    } else {
                        Text("ACTUALIZAR PERFIL", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
                    }
                }

                if (uiState is PerfilState.Error) {
                    Text(
                        text = (uiState as PerfilState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (uiState is PerfilState.Success) {
                    Text(
                        text = "¡Perfil actualizado con éxito!",
                        color = Color.Green,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

private fun uriToFile(uri: Uri, context: android.content.Context): File? {
    val contentResolver = context.contentResolver ?: return null
    val filePath = context.applicationInfo.dataDir + File.separator + "temp_image_${System.currentTimeMillis()}.jpg"
    val file = File(filePath)
    try {
        val inputStream = contentResolver.openInputStream(uri) ?: return null
        val outputStream = FileOutputStream(file)
        val buffer = ByteArray(1024)
        var len: Int
        while (inputStream.read(buffer).also { len = it } > 0) {
            outputStream.write(buffer, 0, len)
        }
        outputStream.close()
        inputStream.close()
        return file
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}
