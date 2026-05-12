package com.tuevento.tueventofinal.ui.eventos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuevento.tueventofinal.data.model.EstadoEvento
import com.tuevento.tueventofinal.data.model.EventoRequest
import com.tuevento.tueventofinal.data.remote.EventoRepository
import com.tuevento.tueventofinal.util.ValidationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class EventoCrearState {
    object Idle : EventoCrearState()
    object Loading : EventoCrearState()
    object Success : EventoCrearState()
    data class Error(val message: String) : EventoCrearState()
}

class EventoCrearViewModel(
    private val repository: EventoRepository = EventoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<EventoCrearState>(EventoCrearState.Idle)
    val uiState: StateFlow<EventoCrearState> = _uiState

    fun crearEvento(
        organizadorId: Long,
        titulo: String,
        descripcion: String,
        fechaInicio: String,
        fechaFin: String,
        lugar: String,
        direccion: String,
        cupoMaximo: String
    ) {
        when {
            !ValidationUtils.isNotBlank(titulo) -> {
                _uiState.value = EventoCrearState.Error("El título es obligatorio")
                return
            }
            !ValidationUtils.isNotBlank(lugar) -> {
                _uiState.value = EventoCrearState.Error("El lugar es obligatorio")
                return
            }
            !ValidationUtils.isValidCupo(cupoMaximo) -> {
                _uiState.value = EventoCrearState.Error("El cupo máximo debe ser un número mayor a 0")
                return
            }
            !ValidationUtils.isNotBlank(fechaInicio) || !ValidationUtils.isNotBlank(fechaFin) -> {
                _uiState.value = EventoCrearState.Error("Las fechas de inicio y fin son obligatorias")
                return
            }
        }

        val request = EventoRequest(
            titulo = titulo.trim(),
            descripcion = descripcion.trim().ifBlank { null },
            fechaInicio = fechaInicio,
            fechaFin = fechaFin,
            lugar = lugar.trim(),
            direccion = direccion.trim().ifBlank { null },
            cupoMaximo = cupoMaximo.toInt(),
            estado = EstadoEvento.BORRADOR // El organizador crea en borrador, luego publica
        )

        viewModelScope.launch {
            _uiState.value = EventoCrearState.Loading
            try {
                val response = repository.createEvento(organizadorId, request)
                if (response.isSuccessful) {
                    _uiState.value = EventoCrearState.Success
                } else {
                    _uiState.value = EventoCrearState.Error(
                        when (response.code()) {
                            400 -> "Datos del evento inválidos. Revisa todos los campos."
                            403 -> "No tienes permisos para crear eventos."
                            else -> "Error al crear evento: ${response.message()}"
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = EventoCrearState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun publicarEvento(eventoId: Long) {
        viewModelScope.launch {
            try {
                repository.publicarEvento(eventoId)
            } catch (e: Exception) {
                _uiState.value = EventoCrearState.Error("Error al publicar: ${e.message}")
            }
        }
    }

    fun resetState() {
        _uiState.value = EventoCrearState.Idle
    }
}
