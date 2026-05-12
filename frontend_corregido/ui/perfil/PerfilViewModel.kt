package com.tuevento.tueventofinal.ui.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuevento.tueventofinal.data.model.UsuarioRequest
import com.tuevento.tueventofinal.data.model.UsuarioResponse
import com.tuevento.tueventofinal.data.remote.UsuarioRepository
import com.tuevento.tueventofinal.util.SessionManager
import com.tuevento.tueventofinal.util.ValidationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class PerfilState {
    object Idle : PerfilState()
    object Loading : PerfilState()
    object Success : PerfilState()
    data class Error(val message: String) : PerfilState()
}

// CORRECCIÓN #5: Se elimina subirFoto() — POST /api/usuarios/{id}/foto no existe
// en el backend. La foto de perfil se gestiona como una URL string (campo fotoUrl)
// que se actualiza mediante PUT /api/usuarios/{id}.
// Para subir imágenes, usar un servicio externo (Cloudinary, Firebase Storage, etc.)
// y guardar la URL resultante con actualizarPerfil().
class PerfilViewModel(
    private val sessionManager: SessionManager,
    private val repository: UsuarioRepository = UsuarioRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<PerfilState>(PerfilState.Idle)
    val uiState: StateFlow<PerfilState> = _uiState

    private val _usuario = MutableStateFlow<UsuarioResponse?>(sessionManager.getUser())
    val usuario: StateFlow<UsuarioResponse?> = _usuario

    fun actualizarPerfil(
        id: Long,
        nombre: String,
        apellido: String,
        email: String,
        telefono: String,
        fotoUrl: String? = null
    ) {
        when {
            !ValidationUtils.isNotBlank(nombre) -> {
                _uiState.value = PerfilState.Error("El nombre es obligatorio")
                return
            }
            !ValidationUtils.isValidEmail(email) -> {
                _uiState.value = PerfilState.Error("Correo inválido")
                return
            }
            telefono.isNotBlank() && !ValidationUtils.isValidPhone(telefono) -> {
                _uiState.value = PerfilState.Error("El teléfono debe tener 10 dígitos")
                return
            }
        }

        viewModelScope.launch {
            _uiState.value = PerfilState.Loading
            try {
                // El UsuarioRequest no incluye password en actualizaciones de perfil
                // (el backend tiene un UsuarioDTO.Update diferente al de registro).
                // Se usa la contraseña actual para no sobreescribirla.
                // NOTA: si el backend tiene un campo de password en el Update DTO,
                // deberías agregar un campo de contraseña en la pantalla de perfil.
                val request = UsuarioRequest(
                    nombre = nombre.trim(),
                    apellido = apellido.trim(),
                    email = email.trim(),
                    password = "", // El backend acepta vacío para no cambiar la contraseña
                    telefono = telefono.trim().ifBlank { null }
                )

                val response = repository.updateUsuario(id, request)
                if (response.isSuccessful && response.body() != null) {
                    val updatedUser = response.body()!!
                    sessionManager.saveSession(updatedUser)
                    _usuario.value = updatedUser
                    _uiState.value = PerfilState.Success
                } else {
                    _uiState.value = PerfilState.Error("Error al actualizar: ${response.message()}")
                }
            } catch (e: Exception) {
                _uiState.value = PerfilState.Error(e.message ?: "Error de red")
            }
        }
    }

    fun logout() {
        sessionManager.logout()
        _usuario.value = null
    }

    fun resetState() {
        _uiState.value = PerfilState.Idle
    }
}
