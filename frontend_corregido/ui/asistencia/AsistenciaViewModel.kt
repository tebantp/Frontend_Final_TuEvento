package com.tuevento.tueventofinal.ui.asistencia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuevento.tueventofinal.data.model.AsistenciaRequest
import com.tuevento.tueventofinal.data.model.AsistenciaResponse
import com.tuevento.tueventofinal.data.model.MetodoRegistro
import com.tuevento.tueventofinal.data.remote.AsistenciaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AsistenciaState {
    object Idle : AsistenciaState()
    object Loading : AsistenciaState()
    data class Success(val asistencia: AsistenciaResponse) : AsistenciaState()
    data class Error(val message: String) : AsistenciaState()
}

class AsistenciaViewModel(
    private val repository: AsistenciaRepository = AsistenciaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AsistenciaState>(AsistenciaState.Idle)
    val uiState: StateFlow<AsistenciaState> = _uiState

    // CORRECCIÓN #3: Antes hacía 2 llamadas a rutas incorrectas:
    //   1. GET /api/qr/codigo/{codigo}   → 404 (ruta no existe)
    //   2. POST /api/asistencias          → 404 (ruta no existe)
    //
    // Ahora usa el endpoint dedicado POST /api/asistencias/validar-qr que:
    //   - Verifica que el QR existe y no está expirado
    //   - Verifica que no fue usado antes
    //   - Registra la asistencia y devuelve AsistenciaResponse
    //   Todo en una sola llamada.
    fun registrarPorQR(codigoQR: String, staffId: Long) {
        if (codigoQR.isBlank()) {
            _uiState.value = AsistenciaState.Error("Código QR vacío")
            return
        }

        viewModelScope.launch {
            _uiState.value = AsistenciaState.Loading
            try {
                val response = repository.validarQR(codigoQR.trim(), staffId)
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = AsistenciaState.Success(response.body()!!)
                } else {
                    val errorMsg = when (response.code()) {
                        404 -> "Código QR no reconocido"
                        409 -> "Este QR ya fue utilizado anteriormente"
                        410 -> "El QR ha expirado"
                        else -> "Error al registrar asistencia (${response.code()})"
                    }
                    _uiState.value = AsistenciaState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = AsistenciaState.Error("Error de conexión: ${e.message}")
            }
        }
    }

    // Registro manual por checkbox (para lista de inscriptos)
    fun registrarManual(inscripcionId: Long, staffId: Long) {
        viewModelScope.launch {
            _uiState.value = AsistenciaState.Loading
            try {
                val request = AsistenciaRequest(
                    inscripcionId = inscripcionId,
                    staffId = staffId,
                    metodo = MetodoRegistro.CHECKBOX
                )
                // CORRECCIÓN #3: Ruta real = POST /api/asistencias/marcar
                val response = repository.marcarAsistencia(request)
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = AsistenciaState.Success(response.body()!!)
                } else {
                    _uiState.value = AsistenciaState.Error(
                        when (response.code()) {
                            409 -> "Asistencia ya registrada"
                            404 -> "Inscripción no encontrada"
                            else -> "Error al registrar (${response.code()})"
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = AsistenciaState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun resetState() {
        _uiState.value = AsistenciaState.Idle
    }
}
