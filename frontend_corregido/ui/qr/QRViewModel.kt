package com.tuevento.tueventofinal.ui.qr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuevento.tueventofinal.data.model.CodigoQRResponse
import com.tuevento.tueventofinal.data.remote.QRRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class QRState {
    object Loading : QRState()
    data class Success(val qr: CodigoQRResponse) : QRState()
    data class Error(val message: String) : QRState()
}

// Este ViewModel está correcto: obtiene el QR de una inscripción existente.
// El QR se generó automáticamente cuando se creó la inscripción.
class QRViewModel(
    private val repository: QRRepository = QRRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<QRState>(QRState.Loading)
    val uiState: StateFlow<QRState> = _uiState

    fun fetchQR(inscripcionId: Long) {
        viewModelScope.launch {
            _uiState.value = QRState.Loading
            try {
                val response = repository.getQRByInscripcion(inscripcionId)
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = QRState.Success(response.body()!!)
                } else {
                    _uiState.value = QRState.Error(
                        when (response.code()) {
                            404 -> "No se encontró el QR para esta inscripción"
                            else -> "Error al cargar QR (${response.code()})"
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = QRState.Error(e.message ?: "Error de red")
            }
        }
    }
}
