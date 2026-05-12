package com.tuevento.tueventofinal.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuevento.tueventofinal.data.model.EstadisticasAdmin
import com.tuevento.tueventofinal.data.model.EventoResponse
import com.tuevento.tueventofinal.data.model.UsuarioResponse
import com.tuevento.tueventofinal.data.remote.EventoRepository
import com.tuevento.tueventofinal.data.remote.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AdminUiState {
    object Loading : AdminUiState()
    data class Success(
        val usuarios: List<UsuarioResponse>,
        val eventos: List<EventoResponse>,
        val stats: EstadisticasAdmin
    ) : AdminUiState()
    data class Error(val message: String) : AdminUiState()
}

sealed class AdminActionState {
    object Idle : AdminActionState()
    object Loading : AdminActionState()
    data class Success(val message: String) : AdminActionState()
    data class Error(val message: String) : AdminActionState()
}

// CORRECCIÓN #7: Se elimina EstadisticasRepository — GET /api/estadisticas/admin
// no existe en el backend. Las estadísticas se calculan desde los datos reales
// obtenidos con getUsuarios() y getEventos().
//
// CORRECCIÓN #4: moderar() ahora usa publicarEvento()/cancelarEvento() en vez de
// moderarEvento(id, estado) que llamaba a PATCH /eventos/{id}/estado (no existe).
class AdminViewModel(
    private val usuarioRepo: UsuarioRepository = UsuarioRepository(),
    private val eventoRepo: EventoRepository = EventoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdminUiState>(AdminUiState.Loading)
    val uiState: StateFlow<AdminUiState> = _uiState

    private val _actionState = MutableStateFlow<AdminActionState>(AdminActionState.Idle)
    val actionState: StateFlow<AdminActionState> = _actionState

    fun loadAdminData() {
        viewModelScope.launch {
            _uiState.value = AdminUiState.Loading
            try {
                val usuarios = usuarioRepo.getUsuarios()
                val eventos = eventoRepo.getEventos()

                // Calcular estadísticas en cliente
                val stats = EstadisticasAdmin(
                    totalUsuarios = usuarios.size,
                    totalEventos = eventos.size,
                    eventosPorEstado = eventos
                        .groupBy { it.estado.name }
                        .mapValues { it.value.size },
                    usuariosPorRol = usuarios
                        .groupBy { it.rol.name }
                        .mapValues { it.value.size }
                )

                _uiState.value = AdminUiState.Success(usuarios, eventos, stats)
            } catch (e: Exception) {
                _uiState.value = AdminUiState.Error("Error de Admin: ${e.message}")
            }
        }
    }

    // CORRECCIÓN #4: Usa los endpoints PATCH correctos del backend
    fun publicarEvento(eventoId: Long) {
        viewModelScope.launch {
            _actionState.value = AdminActionState.Loading
            try {
                val response = eventoRepo.publicarEvento(eventoId)
                if (response.isSuccessful) {
                    _actionState.value = AdminActionState.Success("Evento publicado")
                    loadAdminData()
                } else {
                    _actionState.value = AdminActionState.Error("Error al publicar evento")
                }
            } catch (e: Exception) {
                _actionState.value = AdminActionState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun cancelarEvento(eventoId: Long) {
        viewModelScope.launch {
            _actionState.value = AdminActionState.Loading
            try {
                val response = eventoRepo.cancelarEvento(eventoId)
                if (response.isSuccessful) {
                    _actionState.value = AdminActionState.Success("Evento cancelado")
                    loadAdminData()
                } else {
                    _actionState.value = AdminActionState.Error("Error al cancelar evento")
                }
            } catch (e: Exception) {
                _actionState.value = AdminActionState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun eliminarEvento(eventoId: Long) {
        viewModelScope.launch {
            _actionState.value = AdminActionState.Loading
            try {
                val response = eventoRepo.deleteEvento(eventoId)
                if (response.isSuccessful) {
                    _actionState.value = AdminActionState.Success("Evento eliminado")
                    loadAdminData()
                } else {
                    _actionState.value = AdminActionState.Error("Error al eliminar evento")
                }
            } catch (e: Exception) {
                _actionState.value = AdminActionState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun eliminarUsuario(usuarioId: Long) {
        viewModelScope.launch {
            _actionState.value = AdminActionState.Loading
            try {
                val response = usuarioRepo.deleteUsuario(usuarioId)
                if (response.isSuccessful) {
                    _actionState.value = AdminActionState.Success("Usuario eliminado")
                    loadAdminData()
                } else {
                    _actionState.value = AdminActionState.Error("Error al eliminar usuario")
                }
            } catch (e: Exception) {
                _actionState.value = AdminActionState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun resetActionState() {
        _actionState.value = AdminActionState.Idle
    }
}
