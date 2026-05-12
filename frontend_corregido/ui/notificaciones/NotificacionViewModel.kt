package com.tuevento.tueventofinal.ui.notificaciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuevento.tueventofinal.data.model.NotificacionResponse
import com.tuevento.tueventofinal.data.remote.NotificacionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class NotificacionState {
    object Loading : NotificacionState()
    data class Success(val notificaciones: List<NotificacionResponse>) : NotificacionState()
    data class Error(val message: String) : NotificacionState()
}

class NotificacionViewModel(
    private val repository: NotificacionRepository = NotificacionRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<NotificacionState>(NotificacionState.Loading)
    val uiState: StateFlow<NotificacionState> = _uiState

    // CORRECCIÓN #11: Badge de no-leídas
    private val _noLeidasCount = MutableStateFlow(0)
    val noLeidasCount: StateFlow<Int> = _noLeidasCount

    fun fetchNotificaciones(usuarioId: Long) {
        viewModelScope.launch {
            _uiState.value = NotificacionState.Loading
            try {
                val list = repository.getNotificacionesByUsuario(usuarioId)
                _uiState.value = NotificacionState.Success(list)
                // Actualizar badge
                _noLeidasCount.value = list.count { !it.leida }
            } catch (e: Exception) {
                _uiState.value = NotificacionState.Error(e.message ?: "Error al cargar notificaciones")
            }
        }
    }

    // CORRECCIÓN #11: Obtener solo no-leídas para el badge en la topbar
    fun fetchNoLeidas(usuarioId: Long) {
        viewModelScope.launch {
            try {
                val noLeidas = repository.getNotificacionesNoLeidas(usuarioId)
                _noLeidasCount.value = noLeidas.size
            } catch (e: Exception) {
                // Silently fail — el badge no es crítico
            }
        }
    }

    // CORRECCIÓN #10: La ruta real es /leer (no /leida)
    fun marcarComoLeida(id: Long, usuarioId: Long) {
        viewModelScope.launch {
            try {
                repository.marcarLeida(id)
                fetchNotificaciones(usuarioId)
            } catch (e: Exception) {
                // Silently fail o mostrar snackbar
            }
        }
    }

    fun eliminarNotificacion(id: Long, usuarioId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteNotificacion(id)
                fetchNotificaciones(usuarioId)
            } catch (e: Exception) {
                _uiState.value = NotificacionState.Error("Error al eliminar: ${e.message}")
            }
        }
    }
}
