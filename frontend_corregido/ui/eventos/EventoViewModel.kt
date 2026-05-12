package com.tuevento.tueventofinal.ui.eventos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuevento.tueventofinal.data.model.EventoResponse
import com.tuevento.tueventofinal.data.remote.EventoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class EventoListState {
    object Loading : EventoListState()
    data class Success(val eventos: List<EventoResponse>) : EventoListState()
    data class Error(val message: String) : EventoListState()
}

// CORRECCIÓN #2: Se elimina la lista estática hardcodeada de 6 eventos de la UDEC.
// Ahora fetchEventos() llama al backend real: GET /api/eventos.
class EventoViewModel(
    private val repository: EventoRepository = EventoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<EventoListState>(EventoListState.Loading)
    val uiState: StateFlow<EventoListState> = _uiState

    init {
        fetchEventos()
    }

    fun fetchEventos() {
        viewModelScope.launch {
            _uiState.value = EventoListState.Loading
            try {
                val eventos = repository.getEventos()
                _uiState.value = EventoListState.Success(eventos)
            } catch (e: Exception) {
                _uiState.value = EventoListState.Error(
                    "Error al cargar eventos: verifica la conexión con el servidor"
                )
            }
        }
    }
}
