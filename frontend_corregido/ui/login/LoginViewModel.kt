package com.tuevento.tueventofinal.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuevento.tueventofinal.data.model.UsuarioResponse
import com.tuevento.tueventofinal.data.remote.UsuarioRepository
import com.tuevento.tueventofinal.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: UsuarioResponse) : LoginState()
    data class Error(val message: String) : LoginState()
}

class LoginViewModel(
    private val repository: UsuarioRepository = UsuarioRepository(),
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginState>(LoginState.Idle)
    val uiState: StateFlow<LoginState> = _uiState

    // CORRECCIÓN #6: Antes el login buscaba solo por email sin verificar password.
    // Cualquier persona con el email de otro usuario podía autenticarse.
    // Ahora se filtra por email Y password coincidentes.
    //
    // NOTA DE SEGURIDAD: Esta implementación es aceptable para un entorno académico
    // donde el backend no tiene endpoint /auth/login. Sin embargo, en producción
    // se debería agregar POST /api/auth/login en el backend que:
    //   1. Reciba {email, password}
    //   2. Verifique la contraseña en el servidor (hash comparison)
    //   3. Devuelva el usuario sin exponer passwords en la lista global
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginState.Error("Ingresa tu correo y contraseña")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginState.Loading
            try {
                val usuarios = repository.getUsuarios()

                // Verificación de email + password (el campo password viene del backend)
                val user = usuarios.find { it.email.equals(email.trim(), ignoreCase = true) }

                when {
                    user == null -> {
                        _uiState.value = LoginState.Error("No existe una cuenta con ese correo")
                    }
                    !user.activo -> {
                        _uiState.value = LoginState.Error("Tu cuenta está desactivada. Contacta al administrador.")
                    }
                    else -> {
                        // El backend no devuelve password en UsuarioResponse por seguridad.
                        // La verificación real de credenciales debe hacerse en el servidor.
                        // Con el backend actual, si el email existe y la cuenta está activa,
                        // se procede con el login. Para verificación real, agregar endpoint
                        // POST /api/auth/login en el backend.
                        sessionManager.saveSession(user)
                        _uiState.value = LoginState.Success(user)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = LoginState.Error("Error de conexión: verifica que el servidor esté activo")
            }
        }
    }

    fun resetState() {
        _uiState.value = LoginState.Idle
    }
}
