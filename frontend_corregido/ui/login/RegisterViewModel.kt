package com.tuevento.tueventofinal.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuevento.tueventofinal.data.model.UsuarioRequest
import com.tuevento.tueventofinal.data.model.UsuarioResponse
import com.tuevento.tueventofinal.data.remote.UsuarioRepository
import com.tuevento.tueventofinal.util.ValidationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    data class Success(val user: UsuarioResponse) : RegisterState()
    data class Error(val message: String) : RegisterState()
}

class RegisterViewModel(
    private val repository: UsuarioRepository = UsuarioRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val uiState: StateFlow<RegisterState> = _uiState

    // CORRECCIÓN #9: Se elimina el parámetro 'rol'. El backend asigna USUARIO a
    // todos los nuevos registros sin importar lo que se envíe en el JSON.
    // Si un usuario necesita ser ORGANIZADOR/STAFF/ADMIN, un administrador debe
    // cambiar su rol desde el panel de administración.
    fun register(
        nombre: String,
        apellido: String,
        email: String,
        password: String,
        telefono: String
    ) {
        // Validaciones alineadas con el backend
        when {
            !ValidationUtils.isNotBlank(nombre) -> {
                _uiState.value = RegisterState.Error("El nombre es obligatorio")
                return
            }
            !ValidationUtils.isNotBlank(apellido) -> {
                _uiState.value = RegisterState.Error("El apellido es obligatorio")
                return
            }
            !ValidationUtils.isValidEmail(email) -> {
                _uiState.value = RegisterState.Error("Ingresa un correo válido")
                return
            }
            !ValidationUtils.isValidPassword(password) -> {
                // CORRECCIÓN #15: Mínimo 6 chars, igual que el backend (@Size min=6)
                _uiState.value = RegisterState.Error("La contraseña debe tener al menos 6 caracteres")
                return
            }
            telefono.isNotBlank() && !ValidationUtils.isValidPhone(telefono) -> {
                _uiState.value = RegisterState.Error("El teléfono debe tener 10 dígitos")
                return
            }
        }

        viewModelScope.launch {
            _uiState.value = RegisterState.Loading
            try {
                val request = UsuarioRequest(
                    nombre = nombre.trim(),
                    apellido = apellido.trim(),
                    email = email.trim().lowercase(),
                    password = password,
                    telefono = telefono.trim().ifBlank { null }
                    // rol NO se envía — el backend asigna USUARIO automáticamente
                )
                val response = repository.createUsuario(request)
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = RegisterState.Success(response.body()!!)
                } else {
                    val errorMsg = when (response.code()) {
                        400 -> "Datos inválidos. Verifica todos los campos."
                        409 -> "Ya existe una cuenta con ese correo electrónico."
                        else -> "Error al registrar (código ${response.code()})"
                    }
                    _uiState.value = RegisterState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = RegisterState.Error("Error de conexión: ${e.message}")
            }
        }
    }

    fun resetState() {
        _uiState.value = RegisterState.Idle
    }
}
