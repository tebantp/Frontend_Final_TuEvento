package com.tuevento.tueventofinal.ui.organizador

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuevento.tueventofinal.data.model.EstadisticasOrganizador
import com.tuevento.tueventofinal.data.model.EventoResponse
import com.tuevento.tueventofinal.data.remote.AsistenciaRepository
import com.tuevento.tueventofinal.data.remote.EventoRepository
import com.tuevento.tueventofinal.data.remote.InscripcionRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class OrganizadorUiState {
    object Loading : OrganizadorUiState()
    data class Success(
        val eventos: List<EventoResponse>,
        val stats: EstadisticasOrganizador
    ) : OrganizadorUiState()
    data class Error(val message: String) : OrganizadorUiState()
}

// CORRECCIÓN #7 y #13: Se elimina EstadisticasRepository y las llamadas
// a getEstadisticasOrganizador() que retornaban 404.
// Las estadísticas se calculan combinando llamadas reales al backend.
class OrganizadorViewModel(
    private val eventoRepo: EventoRepository = EventoRepository(),
    private val inscripcionRepo: InscripcionRepository = InscripcionRepository(),
    private val asistenciaRepo: AsistenciaRepository = AsistenciaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<OrganizadorUiState>(OrganizadorUiState.Loading)
    val uiState: StateFlow<OrganizadorUiState> = _uiState

    fun loadDashboard(organizadorId: Long) {
        viewModelScope.launch {
            _uiState.value = OrganizadorUiState.Loading
            try {
                val eventos = eventoRepo.getEventosByOrganizador(organizadorId)

                var totalInscritos = 0
                var totalAsistentes = 0

                val jobs = eventos.map { evento ->
                    async {
                        val inscritos = runCatching {
                            inscripcionRepo.getInscripcionesByEvento(evento.id).size
                        }.getOrDefault(0)

                        val asistentes = runCatching {
                            asistenciaRepo.getAsistenciasByEvento(evento.id).count { it.presente }
                        }.getOrDefault(0)

                        Pair(inscritos, asistentes)
                    }
                }

                jobs.forEach { deferred ->
                    val (i, a) = deferred.await()
                    totalInscritos += i
                    totalAsistentes += a
                }

                val stats = EstadisticasOrganizador(
                    totalEventos = eventos.size,
                    totalInscritos = totalInscritos,
                    totalAsistentes = totalAsistentes,
                    tasaAsistencia = if (totalInscritos > 0)
                        (totalAsistentes.toFloat() / totalInscritos) * 100f else 0f,
                    eventosPorEstado = eventos
                        .groupBy { it.estado.name }
                        .mapValues { it.value.size }
                )

                _uiState.value = OrganizadorUiState.Success(eventos, stats)
            } catch (e: Exception) {
                _uiState.value = OrganizadorUiState.Error("Error al cargar dashboard: ${e.message}")
            }
        }
    }

    fun publicarEvento(eventoId: Long, organizadorId: Long) {
        viewModelScope.launch {
            try {
                eventoRepo.publicarEvento(eventoId)
                loadDashboard(organizadorId)
            } catch (e: Exception) {
                _uiState.value = OrganizadorUiState.Error("Error al publicar: ${e.message}")
            }
        }
    }

    fun cancelarEvento(eventoId: Long, organizadorId: Long) {
        viewModelScope.launch {
            try {
                eventoRepo.cancelarEvento(eventoId)
                loadDashboard(organizadorId)
            } catch (e: Exception) {
                _uiState.value = OrganizadorUiState.Error("Error al cancelar: ${e.message}")
            }
        }
    }

    fun eliminarEvento(eventoId: Long, organizadorId: Long) {
        viewModelScope.launch {
            try {
                eventoRepo.deleteEvento(eventoId)
                loadDashboard(organizadorId)
            } catch (e: Exception) {
                _uiState.value = OrganizadorUiState.Error("Error al eliminar: ${e.message}")
            }
        }
    }
}
