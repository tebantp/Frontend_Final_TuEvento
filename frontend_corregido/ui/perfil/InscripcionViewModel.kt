package com.tuevento.tueventofinal.ui.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuevento.tueventofinal.data.model.InscripcionResponse
import com.tuevento.tueventofinal.data.remote.InscripcionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class InscripcionListState {
    object Loading : InscripcionListState()
    data class Success(val inscripciones: List<InscripcionResponse>) : InscripcionListState()
    data class Error(val message: String) : InscripcionListState()
}

sealed class InscripcionCancelState {
    object Idle : InscripcionCancelState()
    object Loading : InscripcionCancelState()
    object Success : InscripcionCancelState()
    data class Error(val message: String) : InscripcionCancelState()
}

class InscripcionViewModel(
    private val repository: InscripcionRepository = InscripcionRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<InscripcionListState>(InscripcionListState.Loading)
    val uiState: StateFlow<InscripcionListState> = _uiState

    private val _cancelState = MutableStateFlow<InscripcionCancelState>(InscripcionCancelState.Idle)
    val cancelState: StateFlow<InscripcionCancelState> = _cancelState

    fun fetchInscripciones(usuarioId: Long) {
        viewModelScope.launch {
            _uiState.value = InscripcionListState.Loading
            try {
                val list = repository.getInscripcionesByUsuario(usuarioId)
                _uiState.value = InscripcionListState.Success(list)
            } catch (e: Exception) {
                _uiState.value = InscripcionListState.Error(e.message ?: "Error al cargar inscripciones")
            }
        }
    }

    // CORRECCIÓN #8: Llama a PATCH /cancelar (no DELETE) para no perder los datos
    fun cancelarInscripcion(inscripcionId: Long, usuarioId: Long) {
        viewModelScope.launch {
            _cancelState.value = InscripcionCancelState.Loading
            try {
                val response = repository.cancelarInscripcion(inscripcionId)
                if (response.isSuccessful) {
                    _cancelState.value = InscripcionCancelState.Success
                    fetchInscripciones(usuarioId) // Refrescar la lista
                } else {
                    _cancelState.value = InscripcionCancelState.Error("Error al cancelar inscripción")
                }
            } catch (e: Exception) {
                _cancelState.value = InscripcionCancelState.Error(e.message ?: "Error de red")
            }
        }
    }

    fun resetCancelState() {
        _cancelState.value = InscripcionCancelState.Idle
    }
}
