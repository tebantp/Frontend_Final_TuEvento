package com.tuevento.tueventofinal.ui.eventos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuevento.tueventofinal.data.model.EventoResponse
import com.tuevento.tueventofinal.data.model.FeedbackRequest
import com.tuevento.tueventofinal.data.model.FeedbackResponse
import com.tuevento.tueventofinal.data.model.InscripcionRequest
import com.tuevento.tueventofinal.data.model.InscripcionResponse
import com.tuevento.tueventofinal.data.remote.EventoRepository
import com.tuevento.tueventofinal.data.remote.FeedbackRepository
import com.tuevento.tueventofinal.data.remote.InscripcionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class EventoDetalleState {
    object Loading : EventoDetalleState()
    data class Success(
        val evento: EventoResponse,
        val feedback: List<FeedbackResponse> = emptyList(),
        val userInscripcion: InscripcionResponse? = null
    ) : EventoDetalleState()
    data class Error(val message: String) : EventoDetalleState()
}

sealed class InscripcionActionState {
    object Idle : InscripcionActionState()
    object Loading : InscripcionActionState()
    data class Success(val message: String) : InscripcionActionState()
    data class Error(val message: String) : InscripcionActionState()
}

class EventoDetalleViewModel(
    private val repository: EventoRepository = EventoRepository(),
    private val feedbackRepo: FeedbackRepository = FeedbackRepository(),
    private val inscripcionRepo: InscripcionRepository = InscripcionRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<EventoDetalleState>(EventoDetalleState.Loading)
    val uiState: StateFlow<EventoDetalleState> = _uiState

    private val _inscripcionAction = MutableStateFlow<InscripcionActionState>(InscripcionActionState.Idle)
    val inscripcionAction: StateFlow<InscripcionActionState> = _inscripcionAction

    fun fetchEvento(eventoId: Long, usuarioId: Long) {
        viewModelScope.launch {
            _uiState.value = EventoDetalleState.Loading
            try {
                val response = repository.getEventoById(eventoId)
                val feedback = runCatching { feedbackRepo.getFeedbackByEvento(eventoId) }.getOrDefault(emptyList())
                val inscripciones = runCatching { inscripcionRepo.getInscripcionesByUsuario(usuarioId) }.getOrDefault(emptyList())
                val userInscripcion = inscripciones.find { it.eventoId == eventoId }

                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = EventoDetalleState.Success(
                        evento = response.body()!!,
                        feedback = feedback,
                        userInscripcion = userInscripcion
                    )
                } else {
                    _uiState.value = EventoDetalleState.Error("Error al cargar el evento")
                }
            } catch (e: Exception) {
                _uiState.value = EventoDetalleState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun inscribirse(eventoId: Long, usuarioId: Long) {
        viewModelScope.launch {
            _inscripcionAction.value = InscripcionActionState.Loading
            try {
                val response = inscripcionRepo.createInscripcion(
                    InscripcionRequest(eventoId = eventoId, usuarioId = usuarioId)
                )
                if (response.isSuccessful && response.body() != null) {
                    _inscripcionAction.value = InscripcionActionState.Success("¡Inscripción exitosa! Revisa tu QR en Mis Inscripciones.")
                    fetchEvento(eventoId, usuarioId) // Refrescar cupos
                } else {
                    val msg = when (response.code()) {
                        409 -> "Ya estás inscrito en este evento."
                        400 -> "No hay cupos disponibles."
                        else -> "Error al inscribirse (${response.code()})"
                    }
                    _inscripcionAction.value = InscripcionActionState.Error(msg)
                }
            } catch (e: Exception) {
                _inscripcionAction.value = InscripcionActionState.Error(e.message ?: "Error de red")
            }
        }
    }

    fun cancelarInscripcion(inscripcionId: Long, eventoId: Long, usuarioId: Long) {
        viewModelScope.launch {
            _inscripcionAction.value = InscripcionActionState.Loading
            try {
                // CORRECCIÓN #8: Llama a PATCH /cancelar, no DELETE
                val response = inscripcionRepo.cancelarInscripcion(inscripcionId)
                if (response.isSuccessful) {
                    _inscripcionAction.value = InscripcionActionState.Success("Inscripción cancelada.")
                    fetchEvento(eventoId, usuarioId)
                } else {
                    _inscripcionAction.value = InscripcionActionState.Error("Error al cancelar inscripción")
                }
            } catch (e: Exception) {
                _inscripcionAction.value = InscripcionActionState.Error(e.message ?: "Error de red")
            }
        }
    }

    fun enviarFeedback(inscripcionId: Long, calificacion: Int, comentario: String, eventoId: Long, usuarioId: Long) {
        viewModelScope.launch {
            try {
                val request = FeedbackRequest(
                    inscripcionId = inscripcionId,
                    calificacion = calificacion,
                    comentario = comentario.ifBlank { null }
                )
                feedbackRepo.createFeedback(request)
                fetchEvento(eventoId, usuarioId) // Refrescar para mostrar el nuevo feedback
            } catch (e: Exception) {
                _inscripcionAction.value = InscripcionActionState.Error("Error al enviar feedback: ${e.message}")
            }
        }
    }

    fun resetActionState() {
        _inscripcionAction.value = InscripcionActionState.Idle
    }
}
