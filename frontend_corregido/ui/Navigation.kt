package com.tuevento.tueventofinal.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tuevento.tueventofinal.data.model.RolUsuario
import com.tuevento.tueventofinal.ui.admin.AdminDashboardScreen
import com.tuevento.tueventofinal.ui.admin.AdminViewModel
import com.tuevento.tueventofinal.ui.asistencia.AsistenciaViewModel
import com.tuevento.tueventofinal.ui.asistencia.ScannerScreen
import com.tuevento.tueventofinal.ui.chat.ChatScreen
import com.tuevento.tueventofinal.ui.chat.ChatViewModel
import com.tuevento.tueventofinal.ui.eventos.EventoCrearScreen
import com.tuevento.tueventofinal.ui.eventos.EventoCrearViewModel
import com.tuevento.tueventofinal.ui.eventos.EventoDetalleScreen
import com.tuevento.tueventofinal.ui.eventos.EventoDetalleViewModel
import com.tuevento.tueventofinal.ui.eventos.EventoListScreen
import com.tuevento.tueventofinal.ui.eventos.EventoViewModel
import com.tuevento.tueventofinal.ui.login.LoginScreen
import com.tuevento.tueventofinal.ui.login.LoginViewModel
import com.tuevento.tueventofinal.ui.login.RegisterScreen
import com.tuevento.tueventofinal.ui.login.RegisterViewModel
import com.tuevento.tueventofinal.ui.login.SplashScreen
import com.tuevento.tueventofinal.ui.notificaciones.NotificacionScreen
import com.tuevento.tueventofinal.ui.notificaciones.NotificacionViewModel
import com.tuevento.tueventofinal.ui.organizador.OrganizadorDashboardScreen
import com.tuevento.tueventofinal.ui.organizador.OrganizadorViewModel
import com.tuevento.tueventofinal.ui.perfil.InscripcionViewModel
import com.tuevento.tueventofinal.ui.perfil.InscripcionesScreen
import com.tuevento.tueventofinal.ui.perfil.PerfilScreen
import com.tuevento.tueventofinal.ui.perfil.PerfilViewModel
import com.tuevento.tueventofinal.ui.qr.QRScreen
import com.tuevento.tueventofinal.ui.qr.QRViewModel
import com.tuevento.tueventofinal.ui.reportes.ReportesScreen
import com.tuevento.tueventofinal.ui.reportes.ReportesViewModel
import com.tuevento.tueventofinal.util.SessionManager

// Roles que tienen acceso al scanner de QR
private val SCANNER_ROLES = setOf(
    RolUsuario.STAFF,
    RolUsuario.ORGANIZADOR,
    RolUsuario.ADMINISTRADOR
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val sessionManager = SessionManager(context)

    NavHost(navController = navController, startDestination = "splash") {

        composable("splash") {
            SplashScreen(
                onNavigate = {
                    if (sessionManager.isLoggedIn()) {
                        val destination = when (sessionManager.getUser()?.rol) {
                            RolUsuario.ADMINISTRADOR -> "admin_dashboard"
                            RolUsuario.ORGANIZADOR   -> "organizador_dashboard"
                            RolUsuario.STAFF         -> "scanner"
                            else                     -> "eventos"
                        }
                        navController.navigate(destination) {
                            popUpTo("splash") { inclusive = true }
                        }
                    } else {
                        navController.navigate("login") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                }
            )
        }

        composable("login") {
            val loginViewModel: LoginViewModel = viewModel(
                factory = GenericViewModelFactory { LoginViewModel(sessionManager = sessionManager) }
            )
            LoginScreen(
                onLoginSuccess = { user ->
                    val destination = when (user.rol) {
                        RolUsuario.ADMINISTRADOR -> "admin_dashboard"
                        RolUsuario.ORGANIZADOR   -> "organizador_dashboard"
                        RolUsuario.STAFF         -> "scanner"
                        else                     -> "eventos"
                    }
                    navController.navigate(destination) { popUpTo("login") { inclusive = true } }
                },
                onNavigateToRegister = { navController.navigate("register") },
                viewModel = loginViewModel
            )
        }

        composable("register") {
            val registerViewModel: RegisterViewModel = viewModel()
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("login") { popUpTo("register") { inclusive = true } }
                },
                onNavigateToLogin = { navController.popBackStack() },
                viewModel = registerViewModel
            )
        }

        // ─── ADMIN ────────────────────────────────────────────────────────────
        composable("admin_dashboard") {
            val user = sessionManager.getUser()
            // CORRECCIÓN #14: Guard de rol — si por alguna razón llegan aquí sin rol ADMIN
            if (user?.rol != RolUsuario.ADMINISTRADOR) {
                navController.navigate("eventos") { popUpTo("admin_dashboard") { inclusive = true } }
                return@composable
            }
            val adminViewModel: AdminViewModel = viewModel()
            AdminDashboardScreen(
                viewModel = adminViewModel,
                onLogout = {
                    sessionManager.logout()
                    navController.navigate("login") { popUpTo(0) { inclusive = true } }
                }
            )
        }

        // ─── ORGANIZADOR ──────────────────────────────────────────────────────
        composable("organizador_dashboard") {
            val user = sessionManager.getUser()
            if (user?.rol != RolUsuario.ORGANIZADOR && user?.rol != RolUsuario.ADMINISTRADOR) {
                navController.navigate("eventos") { popUpTo("organizador_dashboard") { inclusive = true } }
                return@composable
            }
            val orgViewModel: OrganizadorViewModel = viewModel()
            OrganizadorDashboardScreen(
                organizadorId = user.id,
                viewModel = orgViewModel,
                onNavigateToCrear = { navController.navigate("crear_evento") },
                onNavigateToScanner = { navController.navigate("scanner") },
                onNavigateToDetalle = { id -> navController.navigate("evento_detalle/$id") },
                onNavigateToReportes = { navController.navigate("reportes") }
            )
        }

        composable("reportes") {
            val user = sessionManager.getUser() ?: run {
                navController.navigate("login") { popUpTo(0) }
                return@composable
            }
            val reportesViewModel: ReportesViewModel = viewModel()
            ReportesScreen(
                organizadorId = user.id,
                onBack = { navController.popBackStack() },
                viewModel = reportesViewModel
            )
        }

        // ─── USUARIO / COMUN ──────────────────────────────────────────────────
        composable("eventos") {
            val user = sessionManager.getUser()
            val eventoViewModel: EventoViewModel = viewModel()
            EventoListScreen(
                userRole = user?.rol?.name ?: "USUARIO",
                onEventoClick = { id -> navController.navigate("evento_detalle/$id") },
                onAddEventoClick = { navController.navigate("crear_evento") },
                onScannerClick = { navController.navigate("scanner") },
                onPerfilClick = { navController.navigate("perfil") },
                viewModel = eventoViewModel
            )
        }

        composable("perfil") {
            val perfilViewModel: PerfilViewModel = viewModel(
                factory = GenericViewModelFactory { PerfilViewModel(sessionManager = sessionManager) }
            )
            PerfilScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate("login") { popUpTo(0) { inclusive = true } }
                },
                viewModel = perfilViewModel
            )
        }

        composable("crear_evento") {
            val user = sessionManager.getUser() ?: run {
                navController.navigate("login") { popUpTo(0) }
                return@composable
            }
            val crearViewModel: EventoCrearViewModel = viewModel()
            EventoCrearScreen(
                organizadorId = user.id,
                onSuccess = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
                viewModel = crearViewModel
            )
        }

        composable(
            "evento_detalle/{eventoId}",
            arguments = listOf(navArgument("eventoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("eventoId") ?: 0L
            val user = sessionManager.getUser() ?: run {
                navController.navigate("login") { popUpTo(0) }
                return@composable
            }
            val detalleViewModel: EventoDetalleViewModel = viewModel()
            EventoDetalleScreen(
                eventoId = id,
                usuarioId = user.id,
                onBack = { navController.popBackStack() },
                viewModel = detalleViewModel,
                onNavigateToChat = { eid -> navController.navigate("chat/$eid") }
            )
        }

        composable(
            "chat/{eventoId}",
            arguments = listOf(navArgument("eventoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("eventoId") ?: 0L
            val user = sessionManager.getUser() ?: run {
                navController.navigate("login") { popUpTo(0) }
                return@composable
            }
            val chatViewModel: ChatViewModel = viewModel()
            ChatScreen(
                eventoId = id,
                usuarioId = user.id,
                onBack = { navController.popBackStack() },
                viewModel = chatViewModel
            )
        }

        composable("notificaciones") {
            val user = sessionManager.getUser() ?: run {
                navController.navigate("login") { popUpTo(0) }
                return@composable
            }
            val notiViewModel: NotificacionViewModel = viewModel()
            NotificacionScreen(
                usuarioId = user.id,
                onBack = { navController.popBackStack() },
                onNavigateToEvento = { eid -> navController.navigate("evento_detalle/$eid") },
                viewModel = notiViewModel
            )
        }

        // ─── SCANNER — CORRECCIÓN #14: Guard de rol RBAC ────────────────────
        // Un usuario USUARIO que manipule la navegación local no puede acceder
        // al scanner porque aquí se verifica su rol real desde SharedPreferences.
        composable("scanner") {
            val user = sessionManager.getUser()

            if (user == null || user.rol !in SCANNER_ROLES) {
                // Redirigir sin dejar esta ruta en el back-stack
                navController.navigate("eventos") {
                    popUpTo("scanner") { inclusive = true }
                }
                return@composable
            }

            val asistenciaViewModel: AsistenciaViewModel = viewModel()
            ScannerScreen(
                staffId = user.id,
                onBack = { navController.popBackStack() },
                viewModel = asistenciaViewModel
            )
        }

        composable(
            "qr/{inscripcionId}",
            arguments = listOf(navArgument("inscripcionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val inscripcionId = backStackEntry.arguments?.getLong("inscripcionId") ?: 0L
            val qrViewModel: QRViewModel = viewModel()
            QRScreen(
                inscripcionId = inscripcionId,
                onBack = { navController.popBackStack() },
                viewModel = qrViewModel
            )
        }

        composable(
            "inscripciones/{usuarioId}",
            arguments = listOf(navArgument("usuarioId") { type = NavType.LongType })
        ) { backStackEntry ->
            val usuarioId = backStackEntry.arguments?.getLong("usuarioId") ?: 0L
            val inscripcionViewModel: InscripcionViewModel = viewModel()
            InscripcionesScreen(
                usuarioId = usuarioId,
                onBack = { navController.popBackStack() },
                onVerQR = { inscripcionId -> navController.navigate("qr/$inscripcionId") },
                viewModel = inscripcionViewModel
            )
        }
    }
}

class GenericViewModelFactory<T : androidx.lifecycle.ViewModel>(
    private val creator: () -> T
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return creator() as T
    }
}
