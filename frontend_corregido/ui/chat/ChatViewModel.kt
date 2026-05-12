package com.tuevento.tueventofinal.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuevento.tueventofinal.data.model.MensajeChatRequest
import com.tuevento.tueventofinal.data.model.MensajeChatResponse
import com.tuevento.tueventofinal.data.remote.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ChatState {
    object Loading : ChatState()
    data class Success(val mensajes: List<MensajeChatResponse>) : ChatState()
    data class Error(val message: String) : ChatState()
}

class ChatViewModel(
    private val repository: ChatRepository = ChatRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatState>(ChatState.Loading)
    val uiState: StateFlow<ChatState> = _uiState

    fun fetchMensajes(eventoId: Long) {
        viewModelScope.launch {
            _uiState.value = ChatState.Loading
            try {
                val mensajes = repository.getMensajesByEvento(eventoId)
                _uiState.value = ChatState.Success(mensajes)
            } catch (e: Exception) {
                _uiState.value = ChatState.Error(e.message ?: "Error al cargar mensajes")
            }
        }
    }

    fun enviarMensaje(remitenteId: Long, eventoId: Long, contenido: String) {
        if (contenido.isBlank()) return
        viewModelScope.launch {
            try {
                val request = MensajeChatRequest(remitenteId, eventoId, contenido.trim())
                val response = repository.enviarMensaje(request)
                if (response.isSuccessful) {
                    fetchMensajes(eventoId)
                }
            } catch (e: Exception) {
                _uiState.value = ChatState.Error("Error al enviar: ${e.message}")
            }
        }
    }

    // CORRECCIÓN #12: Se añade la función para editar mensajes usando el endpoint correcto
    // PATCH /api/chat/{id}?contenido=
    fun editarMensaje(mensajeId: Long, nuevoContenido: String, eventoId: Long) {
        if (nuevoContenido.isBlank()) return
        viewModelScope.launch {
            try {
                val response = repository.editarMensaje(mensajeId, nuevoContenido.trim())
                if (response.isSuccessful) {
                    fetchMensajes(eventoId)
                }
            } catch (e: Exception) {
                _uiState.value = ChatState.Error("Error al editar: ${e.message}")
            }
        }
    }

    fun eliminarMensaje(mensajeId: Long, eventoId: Long) {
        viewModelScope.launch {
            try {
                repository.eliminarMensaje(mensajeId)
                fetchMensajes(eventoId)
            } catch (e: Exception) {
                _uiState.value = ChatState.Error("Error al eliminar: ${e.message}")
            }
        }
    }
}
